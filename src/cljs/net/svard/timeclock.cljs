(ns ^:figwheel-always net.svard.timeclock
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljsjs.react-bootstrap]
            [cljsjs.moment] 
            [net.svard.timeclock.parser :as parser]
            [net.svard.timeclock.utils :as utils]
            [net.svard.timeclock.route :as route]
            [net.svard.timeclock.date :as date]))

(enable-console-print!)

(def init-data
  {:app/route '[:app/home _]
   :app/home {:home/date (date/new-date)}
   :app/stats {}})

(defn on-js-reload []
  (println "Reload"))

(def navbar (js/React.createFactory js/ReactBootstrap.Navbar))
(def navbar-header (js/React.createFactory js/ReactBootstrap.Navbar.Header))
(def navbar-brand (js/React.createFactory js/ReactBootstrap.Navbar.Brand))
(def nav (js/React.createFactory js/ReactBootstrap.Nav))
(def nav-item (js/React.createFactory js/ReactBootstrap.NavItem))
(def page-header (js/React.createFactory js/ReactBootstrap.PageHeader))

(defui Header
  Object
  (render [this]
    (let [{:keys [brand] :as props} (om/props this)]
      (navbar #js {:fixedTop true}
        (navbar-header nil
          (navbar-brand nil
            (dom/a #js {:href "#/"}
              (dom/img #js {:src "images/logo.png"
                            :height "24px"
                            :width "24px"}))))
        (nav #js {:pullRight true}
          (nav-item #js {:href "#/"} "Home")
          (nav-item #js {:href "#/stats"} "Statistics"))))))

(def header (om/factory Header))

(defui Root
  static om/IQueryParams
  (params [this]
    {:route/data []})
  
  static om/IQuery
  (query [this]
    '[:app/route
      {:route/data ?route/data}])

  Object
  (componentWillMount [this]
    (route/start-router this))
  
  (render [this]
    (let [{:keys [app/route route/data]} (om/props this)
          active-component (get route/route->factory (first route))]
      (dom/div #js {:style #js {:paddingTop "70px"}}
        (header {:brand "Timeclock"})
        (dom/div #js {:className "container"}
          (page-header nil
            (dom/span nil "Timeclock ")
            (dom/small nil "Ericsson"))
          (active-component data))))))

(def reconciler
  (om/reconciler
    {:state init-data
     :parser (om/parser {:read parser/read :mutate parser/mutate})
     :remotes [:remote]
     ;; :send (utils/transit-post "http://localhost:8086/api")
     :send (utils/transit-post "/api")
     }))

(om/add-root! reconciler Root (gdom/getElement "app"))
