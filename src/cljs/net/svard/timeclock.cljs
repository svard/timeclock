(ns ^:figwheel-always net.svard.timeclock
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljsjs.react-bootstrap]
            [cljsjs.moment]
            [net.svard.timeclock.app :as app]
            [net.svard.timeclock.utils :as utils]
            [net.svard.timeclock.date :as date]))

(enable-console-print!)

(def init-data
  {:app/date {:year (date/year (js/moment)) :week (date/week (js/moment))}})

(defn on-js-reload []
  (println "Reload"))

(defmulti read om/dispatch)

(defmethod read :app/date
  [{:keys [query state]} key _]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

(defmethod read :timeclock/reports
  [{:keys [query state ast]} key params]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)
     :remote ast}))

(defmulti mutate om/dispatch)

(defmethod mutate 'week/increment
  [{:keys [state component]} _ _]
  {:value {:keys [:app/date]}
   :action (fn []
             (println component)
             (swap! state update-in [:app/date] date/incr-week)
             (om/update-query! component (fn [q]
                                           (update q :params date/incr-week))))})

(defmethod mutate 'week/decrement
  [{:keys [state component]} _ _]
  {:value {:keys [:app/date]}
   :action (fn []
             (swap! state update-in [:app/date] date/decr-week)
             (om/update-query! component (fn [q]
                                           (update q :params date/decr-week))))})

(def reconciler
  (om/reconciler
    {:state init-data
     :parser (om/parser {:read read :mutate mutate})
     :remotes [:remote]
     ;; :send (utils/transit-post "http://localhost:8080/api")
     :send (utils/transit-post "/api")
     }))

(om/add-root! reconciler app/App (gdom/getElement "app"))
