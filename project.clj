(defproject clojure-graceful-shutdown "0.1.0-SNAPSHOT"
  :description "This project has the goal to exemplify how to graceful shutdown the clojure project"
  :url "https://github.com/dhakamada/clojure-graceful-shutdown"

  :dependencies [[com.climate/squeedo "1.1.2" :exclusions [org.clojure/tools.reader]]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "0.4.500"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]]

  :profiles {:web {
                   :jar-name     "clojure-graceful-shutdown-web.jar"
                   :uberjar-name "clojure-graceful-shutdown-web-standalone.jar"
                   :main clojure-graceful-shutdown.web
                   :uberjar {:aot :all}}
             :worker {
                   :jar-name     "clojure-graceful-shutdown-worker.jar"
                   :uberjar-name "clojure-graceful-shutdown-worker-standalone.jar"
                   :main clojure-graceful-shutdown.worker
                   :uberjar {:aot [clojure-graceful-shutdown.worker clojure.tools.logging.impl]}}})

