;; example for worker
(ns clojure-graceful-shutdown.worker
  (:gen-class)
  (:require
    [cheshire.core :as json]
    [clojure.core.async :refer [put! take!]]
    [clojure-graceful-shutdown.core :as core]
    [com.climate.squeedo.sqs-consumer :as sqs])
  (:import com.amazonaws.client.builder.AwsClientBuilder$EndpointConfiguration
           com.amazonaws.services.sqs.AmazonSQSClientBuilder))

(defonce consumers (atom []))

(def ^:dynamic *process?* true)
(def ^:private sqs-queue-names ["GracefulShutdownQueue"])
(def ^:private sqs-host "http://localhost:4576")
(def ^:private sqs-region "us-east-1")
(def ^:private time-limit 30000)

(defn compute
  [message done-channel]
  (let [parsed-message (json/parse-string (:body message) true)
        value (:value parsed-message)]
    (prn (str "Start consume message - " value))
    (Thread/sleep 15000)
    (prn (str "Done message - " value))
    (put! done-channel message)))

(defn stop-comsumer!
  "Destroy the Jetty context and stop the SQS consumers"
  []
  (doseq [consumer @consumers]
    (sqs/stop-consumer @consumer)
    (reset! consumer {})))

(defn shutdown-hook!
  "Steps to shutdown hook
  1. Stop consumer
  2. Sleep
  3. Change *process?* variable to stop loop"
  []
  (prn "Stop to consume messages")
  (stop-comsumer!)
  (prn "Always wait for a limited time to 'try' to ensure that the processes are completed")
  (Thread/sleep time-limit)
  (prn "Shutdown Hook is done!")
  (alter-var-root #'*process?* (constantly 'false)))

(defn- localstack-client
  []
  (-> (AmazonSQSClientBuilder/standard)
      (.withEndpointConfiguration
        (AwsClientBuilder$EndpointConfiguration. sqs-host sqs-region))
      (.build)))

(defn -main [& _args]
  (prn (str "Running worker with PID - " (core/pid)))

  ;; ShutdownHook handles the SIGTERM and waits for the thread end
  (.addShutdownHook (Runtime/getRuntime) (Thread. shutdown-hook!))

  (doseq [sqs-queue-name sqs-queue-names]
    (let [consumer (atom {})]
      (swap! consumers conj consumer)
      (swap! consumer merge (sqs/start-consumer sqs-queue-name
                                                compute
                                                :num-listeners 1
                                                :dequeue-limit 20
                                                :message-channel-size 1
                                                :num-workers 1
                                                :max-concurrent-work 1
                                                :client (localstack-client)))))

  (loop []
    (when *process?*
      (do
        (prn "Processing")
        (Thread/sleep 10000)
        (recur)))))