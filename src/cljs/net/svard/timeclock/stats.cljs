(ns ^:figwheel-always net.svard.timeclock.stats
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.component.stats-table :refer [stats-table]]
            [net.svard.timeclock.component.stats-row :refer [Row]]))

(enable-console-print!)

(defui Stats
  static om/IQuery
  (query [this]
    [{:stats/content (om/get-query Row)}])
  
  Object
  (render [this]
    (let [{:keys [stats/content]} (om/props this)]
      (stats-table content))))
