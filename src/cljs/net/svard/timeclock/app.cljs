(ns ^:figwheel-always net.svard.timeclock.app
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.component.table :refer [report-table Row]]
            [net.svard.timeclock.date :as date]
            [cljsjs.react-bootstrap]))

(enable-console-print!)

(def navbar (js/React.createFactory js/ReactBootstrap.Navbar))
(def navbar-header (js/React.createFactory js/ReactBootstrap.Navbar.Header))
(def navbar-brand (js/React.createFactory js/ReactBootstrap.Navbar.Brand))
(def page-header (js/React.createFactory js/ReactBootstrap.PageHeader))
(def pager (js/React.createFactory js/ReactBootstrap.Pager))
(def page-item (js/React.createFactory js/ReactBootstrap.PageItem))

(defui TopNav
  Object
  (render [this]
    (let [{:keys [brand] :as props} (om/props this)]
      (navbar #js {:fixedTop true}
        (navbar-header nil
          (navbar-brand nil
            (dom/span nil brand)))))))

(def topnav (om/factory TopNav))

(defui App
  static om/IQueryParams
  (params [this]
    (let [now (js/moment)]
      {:year (date/year now) :week (date/week now)}))
  
  static om/IQuery
  (query [this]
    `[({:timeclock/reports ~(om/get-query Row)} {:year ~'?year :week ~'?week}) :app/date])
  
  Object
  (next-week [this]
    (om/transact! this '[(week/increment)])
    ;; (om/update-query! this (fn [q]
    ;;                          (update q :params date/incr-week)))
    )

  (previous-week [this]
    (om/transact! this '[(week/decrement)])
    ;; (om/update-query! this (fn [q]
    ;;                          (update q :params date/decr-week)))
    )
  
  (render [this]
    (let [{:keys [timeclock/reports app/date]} (om/props this)]
      (dom/div #js {:style #js {:paddingTop "70px"}}
        (topnav {:brand "Timeclock"})
        (dom/div #js {:className "container"}
          (page-header nil
            (dom/span nil "Timeclock ")
            (dom/small nil "Ericsson"))
          (dom/h4 #js {:className "table-header"} (str "Week " (:week date) " " (:year date)))
          (report-table reports)
          (pager nil
            (page-item #js {:onSelect #(.previous-week this)}
              (dom/i #js {:className "glyphicon glyphicon-chevron-left"}))
            (page-item #js {:onSelect #(.next-week this)}
              (dom/i #js {:className "glyphicon glyphicon-chevron-right"}))))))))

(def app (om/factory App))
