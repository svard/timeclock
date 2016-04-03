(ns net.svard.timeclock.report
  (:refer-clojure :exclude [get])
  (:require [monger.collection :as mc]
            [monger.joda-time]
            [clj-time.coerce :as coerce]
            [clj-time.format :as format])
  (:import [org.bson.types ObjectId]))

(defonce coll "reports")
(def date-time-no-ms (format/formatters :date-time-no-ms))

(defn- string->object-id [object-id]
  (try
    (ObjectId. ^String object-id)
    (catch Exception e nil)))

(defn object-id->str [object-id]
  (str object-id))

(defn get [db id]
  (when-let [object-id (string->object-id id)]
      (-> (mc/find-map-by-id db coll object-id)
          (update :_id object-id->str))))

(defn get-by-week [db year week]
  (->> (mc/aggregate db coll [{"$project" {:total "$total"
                                           :arrival "$arrival"
                                           :leave "$leave"
                                           :lunch "$lunch"
                                           :week {"$week" "$arrival"}
                                           :year {"$year" "$arrival"}}}
                              {"$match" {:week week
                                         :year year}}
                              {"$project" {:total "$total"
                                           :arrival "$arrival"
                                           :leave "$leave"
                                           :lunch "$lunch"}}
                              {"$sort" {"arrival" 1}}])
       (map #(update % :_id object-id->str))))
