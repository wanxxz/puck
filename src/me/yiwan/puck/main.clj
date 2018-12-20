(ns me.yiwan.puck.main
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [me.raynes.fs :as fs]
            me.yiwan.puck.check
            me.yiwan.puck.conf
            [me.yiwan.puck.generate :refer [generate-content]]
            me.yiwan.puck.init
            [mount.core :as mount]))

;; conf used at compile time
(mount/start #'me.yiwan.puck.conf/conf)

(defn usage [options-summary]
  (->> ["Puck, a simple markdown bloging tool"
        ""
        "Usage: puck [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  start     Start application"
        "  init      Initialize"
        "  check     Check files, directories"
        "  generate  Generate post, pages"
        ""]
       (string/join \newline)))

(def cli-options [["-w" "--working-directory path" "Wroking directory"
                   :default (.getAbsolutePath fs/*cwd*)
                   :parse-fn #(.getAbsolutePath (io/file %))
                   :validate [#(fs/directory? %) "not a directory"]]
                  ["-h" "--help"]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit-message (usage summary)}
      errors
      {:exit-message (error-msg errors)}
      (empty? arguments)
      {:exit-message "missing a action: (start init check)"}
      (and (= 1 (count arguments))
           (#{"start" "init" "check" "generate"} (first arguments)))
      {:action (first arguments) :options options}
      :else
      {:exit-message (usage summary)})))

(defn -main [& args]
  (let [{:keys [action options exit-message]} (validate-args args)]
    (if exit-message
      (println exit-message)
      (try
        (case action
          "start"
          (-> (mount/except [#'me.yiwan.puck.init/init
                             #'me.yiwan.puck.check/check])
              (mount/with-args options)
              mount/start)
          "init"
          (-> (mount/only [#'me.yiwan.puck.conf/conf
                           #'me.yiwan.puck.init/init])
              (mount/with-args options)
              mount/start)
          "check"
          (-> (mount/only [#'me.yiwan.puck.conf/conf
                           #'me.yiwan.puck.check/check])
              (mount/with-args options)
              mount/start)
          "generate"
          (do (-> (mount/only [#'me.yiwan.puck.conf/conf])
                  (mount/with-args options)
                  mount/start)
              (generate-content :page)
              (generate-content :post)))
        (catch Exception e
          (throw e))))))
