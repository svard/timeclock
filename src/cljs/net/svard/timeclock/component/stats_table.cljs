(ns ^:figwheel-always net.svard.timeclock.component.stats-table
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.component.stats-row :refer [row]]
            [cljsjs.react-bootstrap]))

(def table (js/React.createFactory js/ReactBootstrap.Table))

(defui StatsTable
  Object
  (render [this]
    (let [rows (om/props this)]
      (table #js {:striped false :bordered false}
        (dom/thead nil
          (dom/tr nil
            (dom/th nil "Year")
            (dom/th nil "Longest day")
            (dom/th nil "Shortest day")
            (dom/th nil "Avg")
            (dom/th nil "Total")))
        (dom/tbody nil
          (map row rows))))))

(def stats-table (om/factory StatsTable))
