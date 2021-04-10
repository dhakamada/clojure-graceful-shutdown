;; example for web server
(ns clojure-graceful-shutdown.web
  (:gen-class)
  (:require [clojure-graceful-shutdown.core :as core])
  (:use ring.adapter.jetty
        ring.middleware.params)
  (:import [org.eclipse.jetty.server.handler StatisticsHandler]))

;; limit wait to shutdown
(def ^:private time-limit-ms-to-shutdown 60000)

(defn handler
  "Simple endpoint path to exemplify"
  [_]
  (. Thread (sleep 20000))
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Graceful Shutdown in Clojure"})

(defn server-config
  "Server config with graceful shutdown handler"
  [server]
  (let [stats-handler (StatisticsHandler.)
        default-handler (.getHandler server)]
    (.setHandler stats-handler default-handler)
    (.setHandler server stats-handler)
    (.setStopTimeout server time-limit-ms-to-shutdown)
    (.setStopAtShutdown server true)))

(defn -main
  []
  ;; show the PID to use to softy kill - $ kill -SIGTERM <PID>
  (prn (str "Start App with PID - " (core/pid)))

  (run-jetty handler {:port         8080
                      :configurator server-config}))