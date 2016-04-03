(ns net.svard.timeclock
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [immuconf.config :as config]
            [clojure.java.io :as io]
            [net.svard.timeclock.component.http :as http]
            [net.svard.timeclock.component.database :as db])
  (:gen-class))

(def config (config/load (io/resource "config.edn") (io/resource "prod.edn")))

(def system
  (component/system-map
    :database (db/new-database config)
    :http (component/using (http/new-http-server config) [:database])))

(defn -main [& args]
  (.addShutdownHook (Runtime/getRuntime) (Thread. #(alter-var-root #'system component/stop)))
  (alter-var-root #'system component/start))
