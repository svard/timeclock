(ns net.svard.timeclock.component.http
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.format-params :refer [wrap-restful-params]]
            [aleph.http :as http]
            [clojure.tools.logging :as log]
            [net.svard.timeclock.resource :as resource]))

(defroutes api-routes
  (POST "/" [] resource/props)
  (POST "/timereport" [] resource/insert-report))

(defroutes app-routes
  (context "/api" [] api-routes)
  (route/resources "/")
  (route/not-found "Not Found"))

(defn- wrap-service [handler services]
  (fn [req]
    (handler (assoc req :services services))))

(defn handler [services]
  (-> app-routes
      (wrap-defaults (assoc site-defaults :security false))
      (wrap-restful-params {:formats [:transit-json :json-kw]
                            :format-options {:transit-json {:handlers
                                                            {"m" resource/joda-time-reader}}}})
      (wrap-service services)
      (wrap-reload)))

(defrecord HttpServer [port]
  component/Lifecycle
  (start [component]
    (let [server (http/start-server (handler {:db (get-in component [:database :db])}) {:port port})]
      (log/info (str "Starting HTTP server on port " port))
      (assoc component :http server)))

  (stop [{:keys [http] :as component}]
    (log/info "Stopping HTTP server")
    (.close http)
    (assoc component :http nil)))

(defn new-http-server [{:keys [http]}]
  (map->HttpServer http))
