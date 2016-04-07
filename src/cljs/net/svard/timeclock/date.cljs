(ns ^:figwheel-always net.svard.timeclock.date
  (:require [cljsjs.moment]))

(defprotocol ISO8601DateFormat
  (to-date-string [this])
  (to-time-string [this]))

(extend-protocol ISO8601DateFormat
  js/Date
  (to-date-string [this]
    (let [month (inc (.getMonth this))
          day (.getDate this)]
      (str (.getFullYear this) "-" (if (< month 10) (str "0" month) month) "-" (if (< day 10) (str "0" day) day))))

  (to-time-string [this]
    (let [hours (.getHours this)
          minutes (.getMinutes this)
          seconds (.getSeconds this)]
      (str (if (< hours 10) (str "0" hours) hours) ":" (if (< minutes 10) (str "0" minutes) minutes) ":" (if (< seconds 10) (str "0" seconds) seconds)))))

(deftype Date [moment]
  ISO8601DateFormat
  (to-date-string [this]
    (.format (.-moment this) "YYYY-MM-DD"))

  (to-time-string [this] 
    (.format (.-moment this) "HH:mm:ss")))

(defn new-date
  ([]
   (->Date (js/moment)))
  ([timestamp]
   (->Date (js/moment timestamp))))

(defn print-date [date]
  (to-date-string date))

(defn print-time [date]
  (to-time-string date))

(defn print-date-time [date]
  (str (print-date date) " " (print-time date)))

(defn second->hour [second]
  (let [hours (/ second 3600)]
    (-> (.round js/Math (* hours 100))
        (/ 100))))

(defn hour->second [hour]
  (* hour 3600))

(defn year [date]
  (.year (.-moment date)))

(defn week [date]
  (.isoWeek (.-moment date)))

(defn weeks-in-year [year]
  (.isoWeeksInYear (js/moment (str year "-01-01"))))

(defn incr-week [{:keys [year week]}]
  (if (> (inc week) (weeks-in-year year))
    {:year (inc year) :week 1}
    {:year year :week (inc week)}))

(defn decr-week [{:keys [year week]}]
  (if (< (dec week) 1)
    {:year (dec year) :week (weeks-in-year (dec year))}
    {:year year :week (dec week)}))

(defn subtract [d1 d2]
  (let [s1 (/ (.valueOf (.-moment d1)) 1000)
        s2 (/ (.valueOf (.-moment d2)) 1000)]
    (- s1 s2)))

(defn equal? [d1 d2]
  (and (= (year d1) (year d2)) (= (week d1) (week d2))))
