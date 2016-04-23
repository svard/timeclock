(ns net.svard.timeclock.auth
  (:require [ring.util.response :as resp]
            [buddy.auth :as auth]
            [crypto.password.scrypt :as password]
            [net.svard.timeclock.account :as account]))

(defn login [request]
  (let [db (get-in request [:services :db])
        username (get-in request [:form-params "username"])
        password (get-in request [:form-params "password"])
        session (:session request)
        found-password (:password (account/find db username))]
    (if (and found-password (password/check password found-password))
      (let [updated-session (assoc session :identity username)]
        (-> (resp/redirect "/")
            (assoc :session updated-session))) 
      (resp/redirect "/login"))))

;; (defn login [request]
;;   (-> (resp/redirect "/")
;;       (assoc :session {:identity "kristofer"})))

(defn allowed? [resource]
  (fn [request]
    (if (auth/authenticated? request)
      resource
      (resp/redirect "/login"))))

(defn logout [request]
  (-> (resp/redirect "/login")
      (assoc :session nil)))
