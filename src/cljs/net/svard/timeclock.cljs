(ns ^:figwheel-always net.svard.timeclock
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljsjs.react-bootstrap]
            [cljsjs.moment]
            [net.svard.timeclock.parser :as parser]
            [net.svard.timeclock.app :as app]
            [net.svard.timeclock.utils :as utils]
            [net.svard.timeclock.date :as date]))

(enable-console-print!)

(def init-data
  {:timeclock/date {:year (date/year (date/new-date)) :week (date/week (date/new-date))}})

(defn on-js-reload []
  (println "Reload"))

(def reconciler
  (om/reconciler
    {:state init-data
     :parser (om/parser {:read parser/read :mutate parser/mutate})
     :remotes [:remote]
     ;; :send (utils/transit-post "http://localhost:8086/api")
     :send (utils/transit-post "/api")
     }))

(om/add-root! reconciler app/App (gdom/getElement "app"))
