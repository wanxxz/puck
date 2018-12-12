(ns me.yiwan.puck.content-spec
  (:require [clojure.pprint :refer [pprint]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as test]
            [clojure.string :refer [blank? join]]
            me.yiwan.puck.content))

;; check post file
(s/def ::not-blank #(not (blank? %)))

(s/def ::file-eof #(not (nil? (re-find #"^(.|\n)*[\n]{2,}$" %))))

(s/def ::post-content
  (s/with-gen
    (s/and string? ::not-blank ::file-eof)
    #(gen/fmap
      (fn [s] (str s (join (take 2 (repeat "\n")))))
      (gen/string))))

;; check post meta
(s/def ::post-meta
  (s/with-gen
    map?
    #(gen/fmap
      (fn [[k v]] (pprint (vec v)) (hash-map k v))
      (gen/tuple (s/gen #{:Metakey :Metavalue}) (s/gen (s/+ (s/and string? ::not-blank)))))))

;; check markdown syntax
(s/def ::markdown-block
  (s/with-gen
    map?
    #(gen/fmap
      (fn [[k v]] (pprint (vec v)) (hash-map k v))
      (gen/tuple (s/gen #{:List :Ordered :Header :Code :Rule :Paragraph}) (s/gen (s/+ (s/and string? ::not-blank)))))))

(s/def ::markdown-blocks (s/+ ::markdown-block))

;; spec function
(s/fdef parse-meta
  :args (s/cat :str ::post-content)
  :ret ::markdown-blocks)

(s/fdef parse-markdown
  :args (s/cat :str ::post-content)
  :ret ::markdown-blocks)

(s/fdef generate-html
  :args (s/cat :blocks ::markdown-blocks)
  :ret string?)

(comment
  ;; meta
  (parse-meta (slurp (file "test" "post" "2018-12-01-meta-only.md")))

  (s/conform ::post-content "words\n\n")
  (s/conform ::post-content "文字\n\n")
  (s/explain ::post-content "もじ\n")
  (s/explain ::post-content "   \n\n")

  (pprint (gen/sample (s/gen ::post-content) 10))
  (pprint (gen/sample (s/gen ::markdown-block) 10))

  (test/summarize-results (test/check `parse-markdown))

  (test/summarize-results (test/check `generate-html)))
