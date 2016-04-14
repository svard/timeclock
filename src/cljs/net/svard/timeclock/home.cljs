(ns ^:figwheel-always net.svard.timeclock.home
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.component.home-table :refer [report-table]]
            [net.svard.timeclock.component.home-row :refer [Row]]
            [net.svard.timeclock.date :as d]
            [cljsjs.react-bootstrap]))

(enable-console-print!)

(def pager (js/React.createFactory js/ReactBootstrap.Pager))
(def page-item (js/React.createFactory js/ReactBootstrap.PageItem))

(defui Home
  static om/IQueryParams
  (params [this]
    (let [now (d/new-date)]
      {:year (d/year now) :week (d/week now)}))
  
  static om/IQuery
  (query [this]
    `[({:home/reports ~(om/get-query Row)} {:year ~'?year :week ~'?week}) :home/date])
  
  Object
  (next-week [this]
    (om/transact! this '[(week/increment)]))

  (previous-week [this]
    (om/transact! this '[(week/decrement)]))
  
  (render [this]
    (let [{:keys [home/reports home/date]} (om/props this)]
      (when (not (nil? date))
        (dom/div nil
          (dom/h4 #js {:className "table-header"} (str "Week " (d/week date) " " (d/year date)))
          (report-table reports)
          (pager nil
            (page-item #js {:className "margin-small"
                            :onSelect #(.previous-week this)}
              (dom/i #js {:className "glyphicon glyphicon-chevron-left"}))
            (page-item #js {:className "margin-small"
                            :onSelect #(.next-week this)
                            :disabled (= date (d/new-date))}
              (dom/i #js {:className "glyphicon glyphicon-chevron-right"}))))))))
