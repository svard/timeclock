(ns ^:figwheel-always net.svard.timeclock.parser
  (:require [om.next :as om]
            [net.svard.timeclock.date :as date]))

(defmulti read om/dispatch)

(defmethod read :timeclock/date
  [{:keys [query state]} key _]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

(defmethod read :timeclock/reports
  [{:keys [query state ast]} key params]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)
     :remote true}))

(defmulti mutate om/dispatch)

(defmethod mutate 'week/increment
  [{:keys [state component]} _ _]
  {:value {:keys [:timeclock/date]}
   :action (fn []
             (swap! state update-in [:timeclock/date] date/incr-week)
             (om/update-query! component (fn [q]
                                           (update q :params date/incr-query-params))))})

(defmethod mutate 'week/decrement
  [{:keys [state component]} _ _]
  {:value {:keys [:timeclock/date]}
   :action (fn []
             (swap! state update-in [:timeclock/date] date/decr-week)
             (om/update-query! component (fn [q]
                                           (update q :params date/decr-query-params))))})
(defmethod mutate 'report/update
  [{:keys [state ref ast]} _ new-props]
  (let [[_ id] ref]
    {:value {:keys [:total]}
     :action (fn [] 
               (swap! state update-in ref merge new-props))
     :remote (update-in ast [:params] assoc :_id id)}))
