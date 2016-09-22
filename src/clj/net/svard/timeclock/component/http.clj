(ns net.svard.timeclock.component.http
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.format-params :refer [wrap-restful-params]]
            [ring.util.response :as resp]
            [aleph.http :as http]
            [aleph.netty :as netty]
            [clojure.tools.logging :as log]
            [ring.middleware.session :refer [wrap-session]]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication]]            
            [clojure.java.io :as io]
            [net.svard.timeclock.resource :as resource]
            [net.svard.timeclock.auth :as auth]))

(def auth-backend (backends/session))

(defroutes api-routes
  (POST "/" [] (auth/allowed? resource/props))
  (GET "/timereport" [] resource/get-reports)
  (GET "/timereport/:id" [] resource/get-one-report)
  (POST "/timereport" [] resource/insert-report))

(defroutes app-routes
  (GET "/" [] (auth/allowed? (io/resource "public/index.html")))
  (GET "/login" [] (io/resource "public/login.html"))
  (POST "/login" [] auth/login)
  (GET "/logout" [] auth/logout)
  (context "/api" [] api-routes)
  (route/resources "/")
  (route/not-found "Not Found"))

(defn- wrap-service [handler services]
  (fn [req]
    (handler (assoc req :services services))))

(defn handler [services]
  (-> app-routes
      (wrap-authentication auth-backend)
      (wrap-session)
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
