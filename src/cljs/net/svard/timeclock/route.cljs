(ns ^:figwheel-always net.svard.timeclock.route
  (:require [om.next :as om]
            [bidi.bidi :as bidi]
            [net.svard.timeclock.home :refer [Home]]
            [net.svard.timeclock.stats :refer [Stats]]
            [goog.events :as events])
  (:import [goog History]
           [goog.history EventType]))

(def routes ["" {"" :app/home
                 "/" :app/home
                 "/stats" :app/stats}])

(def route->component
  {:app/home Home
   :app/stats Stats})

(def route->factory
  (zipmap (keys route->component)
    (map om/factory (vals route->component))))

(def history (History.))

(defn- match-route [component token]
  (let [{:keys [handler]} (bidi/match-route routes token)]
    (when (not (nil? handler))
      (om/transact! component `[(change/route {:route ~handler})]))))

(defn- listen [component]
  (events/listen history EventType.NAVIGATE
    (fn [e]
      (println "route change")
      (match-route component (.-token e)))))

(defn start-router [component]
  (listen component)
  (.setEnabled history true)
  ;; (match-route component (.getToken history))
  )

(defn get-route []
  (:handler (bidi/match-route routes (.getToken history))))
