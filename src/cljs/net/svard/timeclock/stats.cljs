(ns ^:figwheel-always net.svard.timeclock.stats
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.date :as date]
            [cljsjs.react-bootstrap]))

(enable-console-print!)

(def table (js/React.createFactory js/ReactBootstrap.Table))

(defn date-cell [{:keys [date time]}]
  (let [hours (date/second->hour time)
        date-str (.format (.-moment date) "MMM Do")]
    (dom/td nil (str date-str " " hours "h"))))

(defui Row
  static om/Ident
  (ident [this {:keys [_id]}]
    [:row/by-id _id])
  
  static om/IQuery
  (query [this]
    [:_id :longest :shortest :avg :sum])

  Object
  (render [this]
    (let [{:keys [_id longest shortest avg sum]} (om/props this)]
      (dom/tr nil
        (dom/td nil _id)
        (date-cell longest)
        (date-cell shortest)
        (dom/td nil (date/second->hour avg))
        (dom/td nil (date/second->hour sum))))))

(def row (om/factory Row))

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

(defui Stats
  static om/IQuery
  (query [this]
    [{:stats/content (om/get-query Row)}])
  
  Object
  (render [this]
    (let [{:keys [stats/content]} (om/props this)]
      (dom/div nil
        (dom/h4 #js {:className "table-header"} "Statistics")
        (stats-table content)))))
