(ns clojure-graceful-shutdown.core
  (:require [clojure.string :as string])
  (:import (java.lang.management ManagementFactory)))

(defn pid
  []
  "Get current process PID"
  (-> (ManagementFactory/getRuntimeMXBean)
      (.getName)
      (string/split #"@")
      (first)))