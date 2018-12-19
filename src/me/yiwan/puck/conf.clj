(ns me.yiwan.puck.conf
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [cprop.core :refer [load-config]]
            [mount.core :refer [args defstate]]))

(defstate conf :start (let [n "conf.edn"
                            a (args)
                            d (some-> a :working-directory)
                            f (some-> d (io/file n))]
                        (cond
                          f (load-config :resource n :file f :merge [{:wd d}])
                          a (load-config :resource n :merge [{:wd d}])
                          :else (load-config :resource n))))
