(ns net.svard.timeclock.resource
  (:require [liberator.core :refer [defresource]]
            [io.clojure.liberator-transit :as lt]
            [cognitect.transit :as transit]
            [om.next.server :as om]
            [clj-time.coerce :as coerce]
            [net.svard.timeclock.report :as report]
            [net.svard.timeclock.parser :as parser])
  (:import [org.joda.time ReadableInstant]))

(def ^:private joda-time-writer
  (transit/write-handler
    (constantly "m")
    (fn [v] (-> ^ReadableInstant v .getMillis))
    (fn [v] (-> ^ReadableInstant v .getMillis .toString))))

(def joda-time-reader
  (transit/read-handler
    (fn [v]
      (coerce/from-long (read-string v)))))

(defresource get-one-report [{:keys [params services] :as request}]
  :available-media-types ["application/transit+json"]
  
  :exists? (fn [_]
             (let [{db :db} services
                   {id :id} params
                   entity (report/get db id)]
               (when (seq entity)
                 {:entity entity})))
  
  :handle-ok (fn [{:keys [entity]}]
               entity)
  
  :as-response (lt/as-response {:handlers {org.joda.time.DateTime joda-time-writer}}))

(defresource get-reports [{:keys [params services] :as request}]
  :available-media-types ["application/transit+json"]
  
  :handle-ok (fn [_]
               (let [{db :db} services
                     {year :year week :week} params]
                 (report/get-by-week db (read-string year) (read-string week))))
  
  :as-response (lt/as-response {:handlers {org.joda.time.DateTime joda-time-writer}}))

(defresource props [{:keys [services body-params] :as request}]
  :available-media-types ["application/transit+json"]

  :allowed-methods [:post]

  :post! (fn [_]
           (let [{db :db} services]
             {:props ((om/parser {:read parser/readf :mutate parser/mutatef}) {:db db} body-params)}))

  :handle-created :props

  :as-response (lt/as-response {:handlers {org.joda.time.DateTime joda-time-writer}}))
