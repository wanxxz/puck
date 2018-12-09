(ns me.yiwan.puck.post
  (:require [clojure.java.io :refer [file]]
            [hiccup.core :as hiccup]
            [instaparse.core :as insta]
            [instaparse.gll :as gll]
            [me.yiwan.puck.conf :refer [conf]]))

(def parser
  (insta/parser
   "<Post> = Meta Content
     <Meta> = (Metamarker Metaline+ Metamarker EOL)
     Metaline = Metakey Colon <Space> Metavalue EOL
     Metakey = #'[0-9a-zA-Z_]+'
     Metavalue = Word
     <Metamarker> = <#'[-]{3}'> EOL
     <Colon> = <':'>
     <Content> = (Paragraph | Header | List | Ordered | Code | Rule)+
     Header = Line Headerline Blankline+
     <Headerline> = h1 | h2
     h1 = '='+
     h2 = '-'+
     List = Listline+ Blankline+
     Listline = Listmarker <Whitespace+> Word (Whitespace Word)* EOL
     <Listmarker> = <'+' | '*' | '-'>
     Ordered = Orderedline+ Blankline+
     Orderedline = Orderedmarker Whitespace* Word (Whitespace Word)* EOL
     <Orderedmarker> = <#'[0-9]+\\.'>
     Code = Codeline+ Blankline+
     Codeline = <Space Space Space Space> (Whitespace | Word)* EOL
     Rule = Ruleline Blankline+
     <Ruleline> = <'+'+ | '*'+ | '-'+>
     Paragraph = Line+ Blankline+
     <Blankline> = Whitespace* EOL
     <Line> = Linepre Word (Whitespace Word)* Linepost EOL
     <Linepre> = (Space (Space (Space)? )? )?
     <Linepost> = Space?
     <Whitespace> = #'(\\ | \\t)+'
     <Space> = ' '
     <Word> = #'\\S+'
     <EOL> = <'\\n'>"))

(defn generate-inlines [str]
  (let [inlines [[#"!\[(\S+)\]\((\S+)\)" (fn [[n href]] (hiccup/html [:img {:src href :alt n}]))]
                 [#"\[(\S+)\]\((\S+)\)"  (fn [[n href]] (hiccup/html [:a {:href href} n]))]
                 [#"`(\S+)`"             (fn [s] (hiccup/html [:code s]))]
                 [#"\*\*(\S+)\*\*"       (fn [s] (hiccup/html [:strong s]))]
                 [#"__(\S+)__"           (fn [s] (hiccup/html [:strong s]))]
                 [#"\*(\S+)\*"           (fn [s] (hiccup/html [:em s]))]
                 [#"_(\S+)_"             (fn [s] (hiccup/html [:em s]))]]

        res (first (filter (complement nil?)
                           (for [[regex func] inlines]
                             (let [groups (re-matches regex str)]
                               (if groups (func (drop 1 groups)))))))]
    (if (nil? res) str res)))

(defn generate-blocks [blocks]
  (reduce str
          (for [b blocks]
            (case (first b)
              :Metaline ""
              :List (hiccup/html [:ul (for [li (drop 1 b)] [:li (apply str (map generate-inlines (drop 1 li)))])])
              :Ordered (hiccup/html [:ol (for [li (drop 1 b)] [:li (apply str (map generate-inlines (drop 1 li)))])])
              :Header (hiccup/html [(first (last b)) (apply str (map generate-inlines (take (- (count b) 2) (drop 1 b))))])
              :Code (hiccup/html [:pre [:code (apply str (interpose "<br />" (for [line (drop 1 b)] (apply str (drop 1 line)))))]])
              :Rule (hiccup/html [:hr])
              :Paragraph (hiccup/html [:p (apply str (map generate-inlines (drop 1 b)))])))))

(defn seek-template-name
  [meta]
  ;; ignore duplicate meta, use last one
  (let [template-meta-name (-> conf :meta :template)
        meta (map #(hash-map :key (-> % second last) :value (-> % last last)) meta)
        res (filter #(= (:key %) template-meta-name) meta)]
    (if (empty? res)
      (println (format "meta not found: %s" template-meta-name))
      (:value (last res)))))

(defn resolve-template-function
  [value]
  (let [s (symbol (str (-> conf :meta :template) "-" value))
        t (ns-resolve 'me.yiwan.puck.template s)]
    (if (nil? t)
      (println (format "template not found: %s" value))
      t)))

(defn parse-meta
  [str]
  (parser str :start :Meta :partial true))

(defn parse-content
  [str]
  (let [res (parser str)]
    (if (instance? gll/failure-type res)
      (print res)
      res)))

(defn generate-html
  [txt]
  (let [meta (parse-meta txt)
        content (-> txt parse-content generate-blocks)
        template-function (some-> meta seek-template-name resolve-template-function)]
    (if (some? template-function)
      (reduce str (template-function content)))))
