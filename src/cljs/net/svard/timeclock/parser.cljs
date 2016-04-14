(ns ^:figwheel-always net.svard.timeclock.parser
  (:require [om.next :as om]
            [net.svard.timeclock.date :as date]
            [net.svard.timeclock.route :as route]))

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state]} key _]
  (let [st @state
        route (first (:app/route st))]
    {:value (get-in st [route key])}))

(defmethod read :app/route
  [{:keys [state]} key _]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

(defmethod read :route/data
  [{:keys [state ast query parser]} _ _]
  (let [st @state
        env {:state state} 
        value (parser env query)
        remote-query (parser env query :remote)]
    {:value value
     :remote (first (:children (om/query->ast remote-query)))}))

(defmethod read :home/reports
  [{:keys [query state ast]} key params]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)
     :remote ast}))

(defmethod read :stats/content
  [{:keys [state ast query]} key _]
  (let [st @state]
    {:value (om/db->tree query (get st key) st)
     :remote ast}))

(defmulti mutate om/dispatch)

(defmethod mutate 'week/increment
  [{:keys [state component]} _ _]
  {:value {:keys [:home/date]}
   :action (fn []
             (swap! state update-in [:app/home :home/date] date/incr-week)
             (om/update-query! component (fn [q]
                                           (update q :params date/incr-query-params))))})

(defmethod mutate 'week/decrement
  [{:keys [state component]} _ _]
  {:value {:keys [:home/date]}
   :action (fn []
             (swap! state update-in [:app/home :home/date] date/decr-week)
             (om/update-query! component (fn [q]
                                           (update q :params date/decr-query-params))))})
(defmethod mutate 'report/update
  [{:keys [state ref ast]} _ new-props]
  (let [[_ id] ref]
    {:value {:keys [:total]}
     :action (fn [] 
               (swap! state update-in ref merge new-props))
     :remote (update-in ast [:params] assoc :_id id)}))

(defmethod mutate 'change/route
  [{:keys [state component]} _ {:keys [route]}]
  {:value {:keys [:app/route]}
   :action (fn []
             (swap! state assoc :app/route `[~route ~'_])
             (om/set-query! component {:params {:route/data (om/get-query (route/route->component route))}}))})
