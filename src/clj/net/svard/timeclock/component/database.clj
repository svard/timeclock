(ns net.svard.timeclock.component.database
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [monger.core :as mg]))

(defrecord Database [host port dbname]
  component/Lifecycle
  (start [component]
    (let [conn (mg/connect {:host host :port port})
          db (mg/get-db conn dbname)]
      (log/info "Starting database")
      (assoc component :db db :conn conn)))

  (stop [{:keys [conn] :as component}]
    (log/info "Stopping database")
    (try
      (mg/disconnect conn)
      (catch Throwable t (log/error "Failed to stop database")))
    (assoc component :db nil :conn nil)))

(defn new-database [{:keys [database]}]
  (map->Database database))
