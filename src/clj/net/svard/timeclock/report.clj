(ns net.svard.timeclock.report
  (:refer-clojure :exclude [get update])
  (:require [monger.collection :as mc]
            [monger.operators :refer [$set]]
            [monger.result :as result]
            [monger.joda-time]
            [clj-time.coerce :as coerce]
            [clj-time.format :as format]
            [clojure.tools.logging :as log])
  (:import [org.bson.types ObjectId]))

(defonce coll "reports")

(defn- string->object-id [object-id]
  (try
    (ObjectId. ^String object-id)
    (catch Exception e nil)))

(defn object-id->str [object-id]
  (str object-id))

(defn get [db id]
  (when-let [object-id (string->object-id id)]
    (-> (mc/find-map-by-id db coll object-id)
        (clojure.core/update :_id object-id->str))))

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
       (map #(clojure.core/update % :_id object-id->str))))

(defn update [db {:keys [_id] :as params}]
  (let [doc (dissoc params :_id)
        success? (-> (mc/update-by-id db coll (ObjectId. ^String _id) {$set doc})
                     (result/updated-existing?))]
    (case success?
      true true
      false (do
              (log/error "Failed to update existing document, id" _id ", doc" doc)
              false))))

(defn stats [db]
  (mc/aggregate db coll [{"$project" {:year {"$year" "$arrival"}
                                      :total "$total"
                                      :arrival "$arrival"}}
                         {"$sort" {"total" 1}}
                         {"$group" {:_id "$year"
                                    :sum {"$sum" "$total"}
                                    :avg {"$avg" "$total"}
                                    :max {"$max" "$total"}
                                    :min {"$min" "$total"}
                                    :shortest {"$first" "$arrival"}
                                    :longest {"$last" "$arrival"}}}
                         {"$project" {:sum "$sum"
                                      :avg "$avg"
                                      :longest {:time "$max" :date "$longest"}
                                      :shortest {:time "$min" :date "$shortest"}}}
                         {"$sort" {"_id" 1}}]))
