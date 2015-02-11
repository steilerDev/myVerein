/**
 * Document   : myVerein.event.js
 * Description:
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

// Underscore.js 1.7.0
(function(){var n=this,t=n._,r=Array.prototype,e=Object.prototype,u=Function.prototype,i=r.push,a=r.slice,o=r.concat,l=e.toString,c=e.hasOwnProperty,f=Array.isArray,s=Object.keys,p=u.bind,h=function(n){return n instanceof h?n:this instanceof h?void(this._wrapped=n):new h(n)};"undefined"!=typeof exports?("undefined"!=typeof module&&module.exports&&(exports=module.exports=h),exports._=h):n._=h,h.VERSION="1.7.0";var g=function(n,t,r){if(t===void 0)return n;switch(null==r?3:r){case 1:return function(r){return n.call(t,r)};case 2:return function(r,e){return n.call(t,r,e)};case 3:return function(r,e,u){return n.call(t,r,e,u)};case 4:return function(r,e,u,i){return n.call(t,r,e,u,i)}}return function(){return n.apply(t,arguments)}};h.iteratee=function(n,t,r){return null==n?h.identity:h.isFunction(n)?g(n,t,r):h.isObject(n)?h.matches(n):h.property(n)},h.each=h.forEach=function(n,t,r){if(null==n)return n;t=g(t,r);var e,u=n.length;if(u===+u)for(e=0;u>e;e++)t(n[e],e,n);else{var i=h.keys(n);for(e=0,u=i.length;u>e;e++)t(n[i[e]],i[e],n)}return n},h.map=h.collect=function(n,t,r){if(null==n)return[];t=h.iteratee(t,r);for(var e,u=n.length!==+n.length&&h.keys(n),i=(u||n).length,a=Array(i),o=0;i>o;o++)e=u?u[o]:o,a[o]=t(n[e],e,n);return a};var v="Reduce of empty array with no initial value";h.reduce=h.foldl=h.inject=function(n,t,r,e){null==n&&(n=[]),t=g(t,e,4);var u,i=n.length!==+n.length&&h.keys(n),a=(i||n).length,o=0;if(arguments.length<3){if(!a)throw new TypeError(v);r=n[i?i[o++]:o++]}for(;a>o;o++)u=i?i[o]:o,r=t(r,n[u],u,n);return r},h.reduceRight=h.foldr=function(n,t,r,e){null==n&&(n=[]),t=g(t,e,4);var u,i=n.length!==+n.length&&h.keys(n),a=(i||n).length;if(arguments.length<3){if(!a)throw new TypeError(v);r=n[i?i[--a]:--a]}for(;a--;)u=i?i[a]:a,r=t(r,n[u],u,n);return r},h.find=h.detect=function(n,t,r){var e;return t=h.iteratee(t,r),h.some(n,function(n,r,u){return t(n,r,u)?(e=n,!0):void 0}),e},h.filter=h.select=function(n,t,r){var e=[];return null==n?e:(t=h.iteratee(t,r),h.each(n,function(n,r,u){t(n,r,u)&&e.push(n)}),e)},h.reject=function(n,t,r){return h.filter(n,h.negate(h.iteratee(t)),r)},h.every=h.all=function(n,t,r){if(null==n)return!0;t=h.iteratee(t,r);var e,u,i=n.length!==+n.length&&h.keys(n),a=(i||n).length;for(e=0;a>e;e++)if(u=i?i[e]:e,!t(n[u],u,n))return!1;return!0},h.some=h.any=function(n,t,r){if(null==n)return!1;t=h.iteratee(t,r);var e,u,i=n.length!==+n.length&&h.keys(n),a=(i||n).length;for(e=0;a>e;e++)if(u=i?i[e]:e,t(n[u],u,n))return!0;return!1},h.contains=h.include=function(n,t){return null==n?!1:(n.length!==+n.length&&(n=h.values(n)),h.indexOf(n,t)>=0)},h.invoke=function(n,t){var r=a.call(arguments,2),e=h.isFunction(t);return h.map(n,function(n){return(e?t:n[t]).apply(n,r)})},h.pluck=function(n,t){return h.map(n,h.property(t))},h.where=function(n,t){return h.filter(n,h.matches(t))},h.findWhere=function(n,t){return h.find(n,h.matches(t))},h.max=function(n,t,r){var e,u,i=-1/0,a=-1/0;if(null==t&&null!=n){n=n.length===+n.length?n:h.values(n);for(var o=0,l=n.length;l>o;o++)e=n[o],e>i&&(i=e)}else t=h.iteratee(t,r),h.each(n,function(n,r,e){u=t(n,r,e),(u>a||u===-1/0&&i===-1/0)&&(i=n,a=u)});return i},h.min=function(n,t,r){var e,u,i=1/0,a=1/0;if(null==t&&null!=n){n=n.length===+n.length?n:h.values(n);for(var o=0,l=n.length;l>o;o++)e=n[o],i>e&&(i=e)}else t=h.iteratee(t,r),h.each(n,function(n,r,e){u=t(n,r,e),(a>u||1/0===u&&1/0===i)&&(i=n,a=u)});return i},h.shuffle=function(n){for(var t,r=n&&n.length===+n.length?n:h.values(n),e=r.length,u=Array(e),i=0;e>i;i++)t=h.random(0,i),t!==i&&(u[i]=u[t]),u[t]=r[i];return u},h.sample=function(n,t,r){return null==t||r?(n.length!==+n.length&&(n=h.values(n)),n[h.random(n.length-1)]):h.shuffle(n).slice(0,Math.max(0,t))},h.sortBy=function(n,t,r){return t=h.iteratee(t,r),h.pluck(h.map(n,function(n,r,e){return{value:n,index:r,criteria:t(n,r,e)}}).sort(function(n,t){var r=n.criteria,e=t.criteria;if(r!==e){if(r>e||r===void 0)return 1;if(e>r||e===void 0)return-1}return n.index-t.index}),"value")};var m=function(n){return function(t,r,e){var u={};return r=h.iteratee(r,e),h.each(t,function(e,i){var a=r(e,i,t);n(u,e,a)}),u}};h.groupBy=m(function(n,t,r){h.has(n,r)?n[r].push(t):n[r]=[t]}),h.indexBy=m(function(n,t,r){n[r]=t}),h.countBy=m(function(n,t,r){h.has(n,r)?n[r]++:n[r]=1}),h.sortedIndex=function(n,t,r,e){r=h.iteratee(r,e,1);for(var u=r(t),i=0,a=n.length;a>i;){var o=i+a>>>1;r(n[o])<u?i=o+1:a=o}return i},h.toArray=function(n){return n?h.isArray(n)?a.call(n):n.length===+n.length?h.map(n,h.identity):h.values(n):[]},h.size=function(n){return null==n?0:n.length===+n.length?n.length:h.keys(n).length},h.partition=function(n,t,r){t=h.iteratee(t,r);var e=[],u=[];return h.each(n,function(n,r,i){(t(n,r,i)?e:u).push(n)}),[e,u]},h.first=h.head=h.take=function(n,t,r){return null==n?void 0:null==t||r?n[0]:0>t?[]:a.call(n,0,t)},h.initial=function(n,t,r){return a.call(n,0,Math.max(0,n.length-(null==t||r?1:t)))},h.last=function(n,t,r){return null==n?void 0:null==t||r?n[n.length-1]:a.call(n,Math.max(n.length-t,0))},h.rest=h.tail=h.drop=function(n,t,r){return a.call(n,null==t||r?1:t)},h.compact=function(n){return h.filter(n,h.identity)};var y=function(n,t,r,e){if(t&&h.every(n,h.isArray))return o.apply(e,n);for(var u=0,a=n.length;a>u;u++){var l=n[u];h.isArray(l)||h.isArguments(l)?t?i.apply(e,l):y(l,t,r,e):r||e.push(l)}return e};h.flatten=function(n,t){return y(n,t,!1,[])},h.without=function(n){return h.difference(n,a.call(arguments,1))},h.uniq=h.unique=function(n,t,r,e){if(null==n)return[];h.isBoolean(t)||(e=r,r=t,t=!1),null!=r&&(r=h.iteratee(r,e));for(var u=[],i=[],a=0,o=n.length;o>a;a++){var l=n[a];if(t)a&&i===l||u.push(l),i=l;else if(r){var c=r(l,a,n);h.indexOf(i,c)<0&&(i.push(c),u.push(l))}else h.indexOf(u,l)<0&&u.push(l)}return u},h.union=function(){return h.uniq(y(arguments,!0,!0,[]))},h.intersection=function(n){if(null==n)return[];for(var t=[],r=arguments.length,e=0,u=n.length;u>e;e++){var i=n[e];if(!h.contains(t,i)){for(var a=1;r>a&&h.contains(arguments[a],i);a++);a===r&&t.push(i)}}return t},h.difference=function(n){var t=y(a.call(arguments,1),!0,!0,[]);return h.filter(n,function(n){return!h.contains(t,n)})},h.zip=function(n){if(null==n)return[];for(var t=h.max(arguments,"length").length,r=Array(t),e=0;t>e;e++)r[e]=h.pluck(arguments,e);return r},h.object=function(n,t){if(null==n)return{};for(var r={},e=0,u=n.length;u>e;e++)t?r[n[e]]=t[e]:r[n[e][0]]=n[e][1];return r},h.indexOf=function(n,t,r){if(null==n)return-1;var e=0,u=n.length;if(r){if("number"!=typeof r)return e=h.sortedIndex(n,t),n[e]===t?e:-1;e=0>r?Math.max(0,u+r):r}for(;u>e;e++)if(n[e]===t)return e;return-1},h.lastIndexOf=function(n,t,r){if(null==n)return-1;var e=n.length;for("number"==typeof r&&(e=0>r?e+r+1:Math.min(e,r+1));--e>=0;)if(n[e]===t)return e;return-1},h.range=function(n,t,r){arguments.length<=1&&(t=n||0,n=0),r=r||1;for(var e=Math.max(Math.ceil((t-n)/r),0),u=Array(e),i=0;e>i;i++,n+=r)u[i]=n;return u};var d=function(){};h.bind=function(n,t){var r,e;if(p&&n.bind===p)return p.apply(n,a.call(arguments,1));if(!h.isFunction(n))throw new TypeError("Bind must be called on a function");return r=a.call(arguments,2),e=function(){if(!(this instanceof e))return n.apply(t,r.concat(a.call(arguments)));d.prototype=n.prototype;var u=new d;d.prototype=null;var i=n.apply(u,r.concat(a.call(arguments)));return h.isObject(i)?i:u}},h.partial=function(n){var t=a.call(arguments,1);return function(){for(var r=0,e=t.slice(),u=0,i=e.length;i>u;u++)e[u]===h&&(e[u]=arguments[r++]);for(;r<arguments.length;)e.push(arguments[r++]);return n.apply(this,e)}},h.bindAll=function(n){var t,r,e=arguments.length;if(1>=e)throw new Error("bindAll must be passed function names");for(t=1;e>t;t++)r=arguments[t],n[r]=h.bind(n[r],n);return n},h.memoize=function(n,t){var r=function(e){var u=r.cache,i=t?t.apply(this,arguments):e;return h.has(u,i)||(u[i]=n.apply(this,arguments)),u[i]};return r.cache={},r},h.delay=function(n,t){var r=a.call(arguments,2);return setTimeout(function(){return n.apply(null,r)},t)},h.defer=function(n){return h.delay.apply(h,[n,1].concat(a.call(arguments,1)))},h.throttle=function(n,t,r){var e,u,i,a=null,o=0;r||(r={});var l=function(){o=r.leading===!1?0:h.now(),a=null,i=n.apply(e,u),a||(e=u=null)};return function(){var c=h.now();o||r.leading!==!1||(o=c);var f=t-(c-o);return e=this,u=arguments,0>=f||f>t?(clearTimeout(a),a=null,o=c,i=n.apply(e,u),a||(e=u=null)):a||r.trailing===!1||(a=setTimeout(l,f)),i}},h.debounce=function(n,t,r){var e,u,i,a,o,l=function(){var c=h.now()-a;t>c&&c>0?e=setTimeout(l,t-c):(e=null,r||(o=n.apply(i,u),e||(i=u=null)))};return function(){i=this,u=arguments,a=h.now();var c=r&&!e;return e||(e=setTimeout(l,t)),c&&(o=n.apply(i,u),i=u=null),o}},h.wrap=function(n,t){return h.partial(t,n)},h.negate=function(n){return function(){return!n.apply(this,arguments)}},h.compose=function(){var n=arguments,t=n.length-1;return function(){for(var r=t,e=n[t].apply(this,arguments);r--;)e=n[r].call(this,e);return e}},h.after=function(n,t){return function(){return--n<1?t.apply(this,arguments):void 0}},h.before=function(n,t){var r;return function(){return--n>0?r=t.apply(this,arguments):t=null,r}},h.once=h.partial(h.before,2),h.keys=function(n){if(!h.isObject(n))return[];if(s)return s(n);var t=[];for(var r in n)h.has(n,r)&&t.push(r);return t},h.values=function(n){for(var t=h.keys(n),r=t.length,e=Array(r),u=0;r>u;u++)e[u]=n[t[u]];return e},h.pairs=function(n){for(var t=h.keys(n),r=t.length,e=Array(r),u=0;r>u;u++)e[u]=[t[u],n[t[u]]];return e},h.invert=function(n){for(var t={},r=h.keys(n),e=0,u=r.length;u>e;e++)t[n[r[e]]]=r[e];return t},h.functions=h.methods=function(n){var t=[];for(var r in n)h.isFunction(n[r])&&t.push(r);return t.sort()},h.extend=function(n){if(!h.isObject(n))return n;for(var t,r,e=1,u=arguments.length;u>e;e++){t=arguments[e];for(r in t)c.call(t,r)&&(n[r]=t[r])}return n},h.pick=function(n,t,r){var e,u={};if(null==n)return u;if(h.isFunction(t)){t=g(t,r);for(e in n){var i=n[e];t(i,e,n)&&(u[e]=i)}}else{var l=o.apply([],a.call(arguments,1));n=new Object(n);for(var c=0,f=l.length;f>c;c++)e=l[c],e in n&&(u[e]=n[e])}return u},h.omit=function(n,t,r){if(h.isFunction(t))t=h.negate(t);else{var e=h.map(o.apply([],a.call(arguments,1)),String);t=function(n,t){return!h.contains(e,t)}}return h.pick(n,t,r)},h.defaults=function(n){if(!h.isObject(n))return n;for(var t=1,r=arguments.length;r>t;t++){var e=arguments[t];for(var u in e)n[u]===void 0&&(n[u]=e[u])}return n},h.clone=function(n){return h.isObject(n)?h.isArray(n)?n.slice():h.extend({},n):n},h.tap=function(n,t){return t(n),n};var b=function(n,t,r,e){if(n===t)return 0!==n||1/n===1/t;if(null==n||null==t)return n===t;n instanceof h&&(n=n._wrapped),t instanceof h&&(t=t._wrapped);var u=l.call(n);if(u!==l.call(t))return!1;switch(u){case"[object RegExp]":case"[object String]":return""+n==""+t;case"[object Number]":return+n!==+n?+t!==+t:0===+n?1/+n===1/t:+n===+t;case"[object Date]":case"[object Boolean]":return+n===+t}if("object"!=typeof n||"object"!=typeof t)return!1;for(var i=r.length;i--;)if(r[i]===n)return e[i]===t;var a=n.constructor,o=t.constructor;if(a!==o&&"constructor"in n&&"constructor"in t&&!(h.isFunction(a)&&a instanceof a&&h.isFunction(o)&&o instanceof o))return!1;r.push(n),e.push(t);var c,f;if("[object Array]"===u){if(c=n.length,f=c===t.length)for(;c--&&(f=b(n[c],t[c],r,e)););}else{var s,p=h.keys(n);if(c=p.length,f=h.keys(t).length===c)for(;c--&&(s=p[c],f=h.has(t,s)&&b(n[s],t[s],r,e)););}return r.pop(),e.pop(),f};h.isEqual=function(n,t){return b(n,t,[],[])},h.isEmpty=function(n){if(null==n)return!0;if(h.isArray(n)||h.isString(n)||h.isArguments(n))return 0===n.length;for(var t in n)if(h.has(n,t))return!1;return!0},h.isElement=function(n){return!(!n||1!==n.nodeType)},h.isArray=f||function(n){return"[object Array]"===l.call(n)},h.isObject=function(n){var t=typeof n;return"function"===t||"object"===t&&!!n},h.each(["Arguments","Function","String","Number","Date","RegExp"],function(n){h["is"+n]=function(t){return l.call(t)==="[object "+n+"]"}}),h.isArguments(arguments)||(h.isArguments=function(n){return h.has(n,"callee")}),"function"!=typeof/./&&(h.isFunction=function(n){return"function"==typeof n||!1}),h.isFinite=function(n){return isFinite(n)&&!isNaN(parseFloat(n))},h.isNaN=function(n){return h.isNumber(n)&&n!==+n},h.isBoolean=function(n){return n===!0||n===!1||"[object Boolean]"===l.call(n)},h.isNull=function(n){return null===n},h.isUndefined=function(n){return n===void 0},h.has=function(n,t){return null!=n&&c.call(n,t)},h.noConflict=function(){return n._=t,this},h.identity=function(n){return n},h.constant=function(n){return function(){return n}},h.noop=function(){},h.property=function(n){return function(t){return t[n]}},h.matches=function(n){var t=h.pairs(n),r=t.length;return function(n){if(null==n)return!r;n=new Object(n);for(var e=0;r>e;e++){var u=t[e],i=u[0];if(u[1]!==n[i]||!(i in n))return!1}return!0}},h.times=function(n,t,r){var e=Array(Math.max(0,n));t=g(t,r,1);for(var u=0;n>u;u++)e[u]=t(u);return e},h.random=function(n,t){return null==t&&(t=n,n=0),n+Math.floor(Math.random()*(t-n+1))},h.now=Date.now||function(){return(new Date).getTime()};var _={"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#x27;","`":"&#x60;"},w=h.invert(_),j=function(n){var t=function(t){return n[t]},r="(?:"+h.keys(n).join("|")+")",e=RegExp(r),u=RegExp(r,"g");return function(n){return n=null==n?"":""+n,e.test(n)?n.replace(u,t):n}};h.escape=j(_),h.unescape=j(w),h.result=function(n,t){if(null==n)return void 0;var r=n[t];return h.isFunction(r)?n[t]():r};var x=0;h.uniqueId=function(n){var t=++x+"";return n?n+t:t},h.templateSettings={evaluate:/<%([\s\S]+?)%>/g,interpolate:/<%=([\s\S]+?)%>/g,escape:/<%-([\s\S]+?)%>/g};var A=/(.)^/,k={"'":"'","\\":"\\","\r":"r","\n":"n","\u2028":"u2028","\u2029":"u2029"},O=/\\|'|\r|\n|\u2028|\u2029/g,F=function(n){return"\\"+k[n]};h.template=function(n,t,r){!t&&r&&(t=r),t=h.defaults({},t,h.templateSettings);var e=RegExp([(t.escape||A).source,(t.interpolate||A).source,(t.evaluate||A).source].join("|")+"|$","g"),u=0,i="__p+='";n.replace(e,function(t,r,e,a,o){return i+=n.slice(u,o).replace(O,F),u=o+t.length,r?i+="'+\n((__t=("+r+"))==null?'':_.escape(__t))+\n'":e?i+="'+\n((__t=("+e+"))==null?'':__t)+\n'":a&&(i+="';\n"+a+"\n__p+='"),t}),i+="';\n",t.variable||(i="with(obj||{}){\n"+i+"}\n"),i="var __t,__p='',__j=Array.prototype.join,"+"print=function(){__p+=__j.call(arguments,'');};\n"+i+"return __p;\n";try{var a=new Function(t.variable||"obj","_",i)}catch(o){throw o.source=i,o}var l=function(n){return a.call(this,n,h)},c=t.variable||"obj";return l.source="function("+c+"){\n"+i+"}",l},h.chain=function(n){var t=h(n);return t._chain=!0,t};var E=function(n){return this._chain?h(n).chain():n};h.mixin=function(n){h.each(h.functions(n),function(t){var r=h[t]=n[t];h.prototype[t]=function(){var n=[this._wrapped];return i.apply(n,arguments),E.call(this,r.apply(h,n))}})},h.mixin(h),h.each(["pop","push","reverse","shift","sort","splice","unshift"],function(n){var t=r[n];h.prototype[n]=function(){var r=this._wrapped;return t.apply(r,arguments),"shift"!==n&&"splice"!==n||0!==r.length||delete r[0],E.call(this,r)}}),h.each(["concat","join","slice"],function(n){var t=r[n];h.prototype[n]=function(){return E.call(this,t.apply(this._wrapped,arguments))}}),h.prototype.value=function(){return this._wrapped},"function"==typeof define&&define.amd&&define("underscore",[],function(){return h})}).call(this);

// bootstrap-timepicker v0.2.5
!function(t,i,e){"use strict";var s=function(i,e){this.widget="",this.$element=t(i),this.defaultTime=e.defaultTime,this.disableFocus=e.disableFocus,this.disableMousewheel=e.disableMousewheel,this.isOpen=e.isOpen,this.minuteStep=e.minuteStep,this.modalBackdrop=e.modalBackdrop,this.orientation=e.orientation,this.secondStep=e.secondStep,this.showInputs=e.showInputs,this.showMeridian=e.showMeridian,this.showSeconds=e.showSeconds,this.template=e.template,this.appendWidgetTo=e.appendWidgetTo,this.showWidgetOnAddonClick=e.showWidgetOnAddonClick,this._init()};s.prototype={constructor:s,_init:function(){var i=this;this.showWidgetOnAddonClick&&(this.$element.parent().hasClass("input-append")||this.$element.parent().hasClass("input-prepend"))?(this.$element.parent(".input-append, .input-prepend").find(".add-on").on({"click.timepicker":t.proxy(this.showWidget,this)}),this.$element.on({"focus.timepicker":t.proxy(this.highlightUnit,this),"click.timepicker":t.proxy(this.highlightUnit,this),"keydown.timepicker":t.proxy(this.elementKeydown,this),"blur.timepicker":t.proxy(this.blurElement,this),"mousewheel.timepicker DOMMouseScroll.timepicker":t.proxy(this.mousewheel,this)})):this.$element.on(this.template?{"focus.timepicker":t.proxy(this.showWidget,this),"click.timepicker":t.proxy(this.showWidget,this),"blur.timepicker":t.proxy(this.blurElement,this),"mousewheel.timepicker DOMMouseScroll.timepicker":t.proxy(this.mousewheel,this)}:{"focus.timepicker":t.proxy(this.highlightUnit,this),"click.timepicker":t.proxy(this.highlightUnit,this),"keydown.timepicker":t.proxy(this.elementKeydown,this),"blur.timepicker":t.proxy(this.blurElement,this),"mousewheel.timepicker DOMMouseScroll.timepicker":t.proxy(this.mousewheel,this)}),this.$widget=this.template!==!1?t(this.getTemplate()).on("click",t.proxy(this.widgetClick,this)):!1,this.showInputs&&this.$widget!==!1&&this.$widget.find("input").each(function(){t(this).on({"click.timepicker":function(){t(this).select()},"keydown.timepicker":t.proxy(i.widgetKeydown,i),"keyup.timepicker":t.proxy(i.widgetKeyup,i)})}),this.setDefaultTime(this.defaultTime)},blurElement:function(){this.highlightedUnit=null,this.updateFromElementVal()},clear:function(){this.hour="",this.minute="",this.second="",this.meridian="",this.$element.val("")},decrementHour:function(){if(this.showMeridian)if(1===this.hour)this.hour=12;else{if(12===this.hour)return this.hour--,this.toggleMeridian();if(0===this.hour)return this.hour=11,this.toggleMeridian();this.hour--}else this.hour<=0?this.hour=23:this.hour--},decrementMinute:function(t){var i;i=t?this.minute-t:this.minute-this.minuteStep,0>i?(this.decrementHour(),this.minute=i+60):this.minute=i},decrementSecond:function(){var t=this.second-this.secondStep;0>t?(this.decrementMinute(!0),this.second=t+60):this.second=t},elementKeydown:function(t){switch(t.keyCode){case 9:case 27:this.updateFromElementVal();break;case 37:t.preventDefault(),this.highlightPrevUnit();break;case 38:switch(t.preventDefault(),this.highlightedUnit){case"hour":this.incrementHour(),this.highlightHour();break;case"minute":this.incrementMinute(),this.highlightMinute();break;case"second":this.incrementSecond(),this.highlightSecond();break;case"meridian":this.toggleMeridian(),this.highlightMeridian()}this.update();break;case 39:t.preventDefault(),this.highlightNextUnit();break;case 40:switch(t.preventDefault(),this.highlightedUnit){case"hour":this.decrementHour(),this.highlightHour();break;case"minute":this.decrementMinute(),this.highlightMinute();break;case"second":this.decrementSecond(),this.highlightSecond();break;case"meridian":this.toggleMeridian(),this.highlightMeridian()}this.update()}},getCursorPosition:function(){var t=this.$element.get(0);if("selectionStart"in t)return t.selectionStart;if(e.selection){t.focus();var i=e.selection.createRange(),s=e.selection.createRange().text.length;return i.moveStart("character",-t.value.length),i.text.length-s}},getTemplate:function(){var t,i,e,s,h,n;switch(this.showInputs?(i='<input type="text" class="bootstrap-timepicker-hour" maxlength="2"/>',e='<input type="text" class="bootstrap-timepicker-minute" maxlength="2"/>',s='<input type="text" class="bootstrap-timepicker-second" maxlength="2"/>',h='<input type="text" class="bootstrap-timepicker-meridian" maxlength="2"/>'):(i='<span class="bootstrap-timepicker-hour"></span>',e='<span class="bootstrap-timepicker-minute"></span>',s='<span class="bootstrap-timepicker-second"></span>',h='<span class="bootstrap-timepicker-meridian"></span>'),n='<table><tr><td><a href="#" data-action="incrementHour"><i class=" arrow_carrot-up"></i></a></td><td class="separator">&nbsp;</td><td><a href="#" data-action="incrementMinute"><i class=" arrow_carrot-up"></i></a></td>'+(this.showSeconds?'<td class="separator">&nbsp;</td><td><a href="#" data-action="incrementSecond"><i class=" arrow_carrot-up"></i></a></td>':"")+(this.showMeridian?'<td class="separator">&nbsp;</td><td class="meridian-column"><a href="#" data-action="toggleMeridian"><i class=" arrow_carrot-up"></i></a></td>':"")+"</tr><tr><td>"+i+'</td> <td class="separator">:</td><td>'+e+"</td> "+(this.showSeconds?'<td class="separator">:</td><td>'+s+"</td>":"")+(this.showMeridian?'<td class="separator">&nbsp;</td><td>'+h+"</td>":"")+'</tr><tr><td><a href="#" data-action="decrementHour"><i class="arrow_carrot-down"></i></a></td><td class="separator"></td><td><a href="#" data-action="decrementMinute"><i class="arrow_carrot-down"></i></a></td>'+(this.showSeconds?'<td class="separator">&nbsp;</td><td><a href="#" data-action="decrementSecond"><i class="arrow_carrot-down"></i></a></td>':"")+(this.showMeridian?'<td class="separator">&nbsp;</td><td><a href="#" data-action="toggleMeridian"><i class="arrow_carrot-down"></i></a></td>':"")+"</tr></table>",this.template){case"modal":t='<div class="bootstrap-timepicker-widget modal hide fade in" data-backdrop="'+(this.modalBackdrop?"true":"false")+'"><div class="modal-header"><a href="#" class="close" data-dismiss="modal">×</a><h3>Pick a Time</h3></div><div class="modal-content">'+n+'</div><div class="modal-footer"><a href="#" class="btn btn-primary" data-dismiss="modal">OK</a></div></div>';break;case"dropdown":t='<div class="bootstrap-timepicker-widget dropdown-menu">'+n+"</div>"}return t},getTime:function(){return""===this.hour?"":this.hour+":"+(1===this.minute.toString().length?"0"+this.minute:this.minute)+(this.showSeconds?":"+(1===this.second.toString().length?"0"+this.second:this.second):"")+(this.showMeridian?" "+this.meridian:"")},hideWidget:function(){this.isOpen!==!1&&(this.$element.trigger({type:"hide.timepicker",time:{value:this.getTime(),hours:this.hour,minutes:this.minute,seconds:this.second,meridian:this.meridian}}),"modal"===this.template&&this.$widget.modal?this.$widget.modal("hide"):this.$widget.removeClass("open"),t(e).off("mousedown.timepicker, touchend.timepicker"),this.isOpen=!1,this.$widget.detach())},highlightUnit:function(){this.position=this.getCursorPosition(),this.position>=0&&this.position<=2?this.highlightHour():this.position>=3&&this.position<=5?this.highlightMinute():this.position>=6&&this.position<=8?this.showSeconds?this.highlightSecond():this.highlightMeridian():this.position>=9&&this.position<=11&&this.highlightMeridian()},highlightNextUnit:function(){switch(this.highlightedUnit){case"hour":this.highlightMinute();break;case"minute":this.showSeconds?this.highlightSecond():this.showMeridian?this.highlightMeridian():this.highlightHour();break;case"second":this.showMeridian?this.highlightMeridian():this.highlightHour();break;case"meridian":this.highlightHour()}},highlightPrevUnit:function(){switch(this.highlightedUnit){case"hour":this.showMeridian?this.highlightMeridian():this.showSeconds?this.highlightSecond():this.highlightMinute();break;case"minute":this.highlightHour();break;case"second":this.highlightMinute();break;case"meridian":this.showSeconds?this.highlightSecond():this.highlightMinute()}},highlightHour:function(){var t=this.$element.get(0),i=this;this.highlightedUnit="hour",t.setSelectionRange&&setTimeout(function(){i.hour<10?t.setSelectionRange(0,1):t.setSelectionRange(0,2)},0)},highlightMinute:function(){var t=this.$element.get(0),i=this;this.highlightedUnit="minute",t.setSelectionRange&&setTimeout(function(){i.hour<10?t.setSelectionRange(2,4):t.setSelectionRange(3,5)},0)},highlightSecond:function(){var t=this.$element.get(0),i=this;this.highlightedUnit="second",t.setSelectionRange&&setTimeout(function(){i.hour<10?t.setSelectionRange(5,7):t.setSelectionRange(6,8)},0)},highlightMeridian:function(){var t=this.$element.get(0),i=this;this.highlightedUnit="meridian",t.setSelectionRange&&(this.showSeconds?setTimeout(function(){i.hour<10?t.setSelectionRange(8,10):t.setSelectionRange(9,11)},0):setTimeout(function(){i.hour<10?t.setSelectionRange(5,7):t.setSelectionRange(6,8)},0))},incrementHour:function(){if(this.showMeridian){if(11===this.hour)return this.hour++,this.toggleMeridian();12===this.hour&&(this.hour=0)}return 23===this.hour?void(this.hour=0):void this.hour++},incrementMinute:function(t){var i;i=t?this.minute+t:this.minute+this.minuteStep-this.minute%this.minuteStep,i>59?(this.incrementHour(),this.minute=i-60):this.minute=i},incrementSecond:function(){var t=this.second+this.secondStep-this.second%this.secondStep;t>59?(this.incrementMinute(!0),this.second=t-60):this.second=t},mousewheel:function(i){if(!this.disableMousewheel){i.preventDefault(),i.stopPropagation();var e=i.originalEvent.wheelDelta||-i.originalEvent.detail,s=null;switch("mousewheel"===i.type?s=-1*i.originalEvent.wheelDelta:"DOMMouseScroll"===i.type&&(s=40*i.originalEvent.detail),s&&(i.preventDefault(),t(this).scrollTop(s+t(this).scrollTop())),this.highlightedUnit){case"minute":e>0?this.incrementMinute():this.decrementMinute(),this.highlightMinute();break;case"second":e>0?this.incrementSecond():this.decrementSecond(),this.highlightSecond();break;case"meridian":this.toggleMeridian(),this.highlightMeridian();break;default:e>0?this.incrementHour():this.decrementHour(),this.highlightHour()}return!1}},place:function(){if(!this.isInline){var e=this.$widget.outerWidth(),s=this.$widget.outerHeight(),h=10,n=t(i).width(),o=t(i).height(),r=t(i).scrollTop(),a=parseInt(this.$element.parents().filter(function(){}).first().css("z-index"),10)+10,d=this.component?this.component.parent().offset():this.$element.offset(),c=this.component?this.component.outerHeight(!0):this.$element.outerHeight(!1),l=this.component?this.component.outerWidth(!0):this.$element.outerWidth(!1),u=d.left,p=d.top;this.$widget.removeClass("timepicker-orient-top timepicker-orient-bottom timepicker-orient-right timepicker-orient-left"),"auto"!==this.orientation.x?(this.picker.addClass("datepicker-orient-"+this.orientation.x),"right"===this.orientation.x&&(u-=e-l)):(this.$widget.addClass("timepicker-orient-left"),d.left<0?u-=d.left-h:d.left+e>n&&(u=n-e-h));var m,g,w=this.orientation.y;"auto"===w&&(m=-r+d.top-s,g=r+o-(d.top+c+s),w=Math.max(m,g)===g?"top":"bottom"),this.$widget.addClass("timepicker-orient-"+w),"top"===w?p+=c:p-=s+parseInt(this.$widget.css("padding-top"),10),this.$widget.css({top:p,left:u,zIndex:a})}},remove:function(){t("document").off(".timepicker"),this.$widget&&this.$widget.remove(),delete this.$element.data().timepicker},setDefaultTime:function(t){if(this.$element.val())this.updateFromElementVal();else if("current"===t){var i=new Date,e=i.getHours(),s=i.getMinutes(),h=i.getSeconds(),n="AM";0!==h&&(h=Math.ceil(i.getSeconds()/this.secondStep)*this.secondStep,60===h&&(s+=1,h=0)),0!==s&&(s=Math.ceil(i.getMinutes()/this.minuteStep)*this.minuteStep,60===s&&(e+=1,s=0)),this.showMeridian&&(0===e?e=12:e>=12?(e>12&&(e-=12),n="PM"):n="AM"),this.hour=e,this.minute=s,this.second=h,this.meridian=n,this.update()}else t===!1?(this.hour=0,this.minute=0,this.second=0,this.meridian="AM"):this.setTime(t)},setTime:function(t,i){if(!t)return void this.clear();var e,s,h,n,o;"object"==typeof t&&t.getMonth?(s=t.getHours(),h=t.getMinutes(),n=t.getSeconds(),this.showMeridian&&(o="AM",s>12&&(o="PM",s%=12),12===s&&(o="PM"))):(o=null!==t.match(/p/i)?"PM":"AM",t=t.replace(/[^0-9\:]/g,""),e=t.split(":"),s=e[0]?e[0].toString():e.toString(),h=e[1]?e[1].toString():"",n=e[2]?e[2].toString():"",s.length>4&&(n=s.substr(4,2)),s.length>2&&(h=s.substr(2,2),s=s.substr(0,2)),h.length>2&&(n=h.substr(2,2),h=h.substr(0,2)),n.length>2&&(n=n.substr(2,2)),s=parseInt(s,10),h=parseInt(h,10),n=parseInt(n,10),isNaN(s)&&(s=0),isNaN(h)&&(h=0),isNaN(n)&&(n=0),this.showMeridian?1>s?s=1:s>12&&(s=12):(s>=24?s=23:0>s&&(s=0),13>s&&"PM"===o&&(s+=12)),0>h?h=0:h>=60&&(h=59),this.showSeconds&&(isNaN(n)?n=0:0>n?n=0:n>=60&&(n=59))),this.hour=s,this.minute=h,this.second=n,this.meridian=o,this.update(i)},showWidget:function(){if(!this.isOpen&&!this.$element.is(":disabled")){this.$widget.appendTo(this.appendWidgetTo);var i=this;t(e).on("mousedown.timepicker, touchend.timepicker",function(t){i.$element.parent().find(t.target).length||i.$widget.is(t.target)||i.$widget.find(t.target).length||i.hideWidget()}),this.$element.trigger({type:"show.timepicker",time:{value:this.getTime(),hours:this.hour,minutes:this.minute,seconds:this.second,meridian:this.meridian}}),this.place(),this.disableFocus&&this.$element.blur(),""===this.hour&&(this.defaultTime?this.setDefaultTime(this.defaultTime):this.setTime("0:0:0")),"modal"===this.template&&this.$widget.modal?this.$widget.modal("show").on("hidden",t.proxy(this.hideWidget,this)):this.isOpen===!1&&this.$widget.addClass("open"),this.isOpen=!0}},toggleMeridian:function(){this.meridian="AM"===this.meridian?"PM":"AM"},update:function(t){this.updateElement(),t||this.updateWidget(),this.$element.trigger({type:"changeTime.timepicker",time:{value:this.getTime(),hours:this.hour,minutes:this.minute,seconds:this.second,meridian:this.meridian}})},updateElement:function(){this.$element.val(this.getTime()).change()},updateFromElementVal:function(){this.setTime(this.$element.val())},updateWidget:function(){if(this.$widget!==!1){var t=this.hour,i=1===this.minute.toString().length?"0"+this.minute:this.minute,e=1===this.second.toString().length?"0"+this.second:this.second;this.showInputs?(this.$widget.find("input.bootstrap-timepicker-hour").val(t),this.$widget.find("input.bootstrap-timepicker-minute").val(i),this.showSeconds&&this.$widget.find("input.bootstrap-timepicker-second").val(e),this.showMeridian&&this.$widget.find("input.bootstrap-timepicker-meridian").val(this.meridian)):(this.$widget.find("span.bootstrap-timepicker-hour").text(t),this.$widget.find("span.bootstrap-timepicker-minute").text(i),this.showSeconds&&this.$widget.find("span.bootstrap-timepicker-second").text(e),this.showMeridian&&this.$widget.find("span.bootstrap-timepicker-meridian").text(this.meridian))}},updateFromWidgetInputs:function(){if(this.$widget!==!1){var t=this.$widget.find("input.bootstrap-timepicker-hour").val()+":"+this.$widget.find("input.bootstrap-timepicker-minute").val()+(this.showSeconds?":"+this.$widget.find("input.bootstrap-timepicker-second").val():"")+(this.showMeridian?this.$widget.find("input.bootstrap-timepicker-meridian").val():"");this.setTime(t,!0)}},widgetClick:function(i){i.stopPropagation(),i.preventDefault();var e=t(i.target),s=e.closest("a").data("action");s&&this[s](),this.update(),e.is("input")&&e.get(0).setSelectionRange(0,2)},widgetKeydown:function(i){var e=t(i.target),s=e.attr("class").replace("bootstrap-timepicker-","");switch(i.keyCode){case 9:if(this.showMeridian&&"meridian"===s||this.showSeconds&&"second"===s||!this.showMeridian&&!this.showSeconds&&"minute"===s)return this.hideWidget();break;case 27:this.hideWidget();break;case 38:switch(i.preventDefault(),s){case"hour":this.incrementHour();break;case"minute":this.incrementMinute();break;case"second":this.incrementSecond();break;case"meridian":this.toggleMeridian()}this.setTime(this.getTime()),e.get(0).setSelectionRange(0,2);break;case 40:switch(i.preventDefault(),s){case"hour":this.decrementHour();break;case"minute":this.decrementMinute();break;case"second":this.decrementSecond();break;case"meridian":this.toggleMeridian()}this.setTime(this.getTime()),e.get(0).setSelectionRange(0,2)}},widgetKeyup:function(t){(65===t.keyCode||77===t.keyCode||80===t.keyCode||46===t.keyCode||8===t.keyCode||t.keyCode>=46&&t.keyCode<=57||t.keyCode>=96&&t.keyCode<=105)&&this.updateFromWidgetInputs()}},t.fn.timepicker=function(i){var e=Array.apply(null,arguments);return e.shift(),this.each(function(){var h=t(this),n=h.data("timepicker"),o="object"==typeof i&&i;n||h.data("timepicker",n=new s(this,t.extend({},t.fn.timepicker.defaults,o,t(this).data()))),"string"==typeof i&&n[i].apply(n,e)})},t.fn.timepicker.defaults={defaultTime:"current",disableFocus:!1,disableMousewheel:!1,isOpen:!1,minuteStep:15,modalBackdrop:!1,orientation:{x:"auto",y:"auto"},secondStep:15,showSeconds:!1,showInputs:!0,showMeridian:!0,template:"dropdown",appendWidgetTo:"body",showWidgetOnAddonClick:!0},t.fn.timepicker.Constructor=s}(jQuery,window,document);

// clndr.min.js v1.2.7
!function(a){"function"==typeof define&&define.amd?define(["jquery","moment"],a):"object"==typeof exports?a(require("jquery"),require("moment")):a(jQuery,moment)}(function(a,b){function c(c,d){if(this.element=c,this.options=a.extend(!0,{},f,d),this.options.events.length&&(this.options.events=this.options.multiDayEvents?this.addMultiDayMomentObjectsToEvents(this.options.events):this.addMomentObjectToEvents(this.options.events)),this.month=this.options.startWithMonth?b(this.options.startWithMonth).startOf("month"):b().startOf("month"),this.options.constraints){if(this.options.constraints.startDate){var g=b(this.options.constraints.startDate);this.month.isBefore(g,"month")&&(this.month.set("month",g.month()),this.month.set("year",g.year()))}if(this.options.constraints.endDate){var h=b(this.options.constraints.endDate);this.month.isAfter(h,"month")&&this.month.set("month",h.month()).set("year",h.year())}}this._defaults=f,this._name=e,this.init()}var d="<div class='clndr-controls'><div class='clndr-control-button'><span class='clndr-previous-button'>previous</span></div><div class='month'><%= month %> <%= year %></div><div class='clndr-control-button rightalign'><span class='clndr-next-button'>next</span></div></div><table class='clndr-table' border='0' cellspacing='0' cellpadding='0'><thead><tr class='header-days'><% for(var i = 0; i < daysOfTheWeek.length; i++) { %><td class='header-day'><%= daysOfTheWeek[i] %></td><% } %></tr></thead><tbody><% for(var i = 0; i < numberOfRows; i++){ %><tr><% for(var j = 0; j < 7; j++){ %><% var d = j + i * 7; %><td class='<%= days[d].classes %>'><div class='day-contents'><%= days[d].day %></div></td><% } %></tr><% } %></tbody></table>",e="clndr",f={template:d,weekOffset:0,startWithMonth:null,clickEvents:{click:null,nextMonth:null,previousMonth:null,nextYear:null,previousYear:null,today:null,onMonthChange:null,onYearChange:null},targets:{nextButton:"clndr-next-button",previousButton:"clndr-previous-button",nextYearButton:"clndr-next-year-button",previousYearButton:"clndr-previous-year-button",todayButton:"clndr-today-button",day:"day",empty:"empty"},classes:{today:"today",event:"event",past:"past",lastMonth:"last-month",nextMonth:"next-month",adjacentMonth:"adjacent-month",inactive:"inactive"},events:[],extras:null,dateParameter:"date",multiDayEvents:null,doneRendering:null,render:null,daysOfTheWeek:null,showAdjacentMonths:!0,adjacentDaysChangeMonth:!1,ready:null,constraints:null,forceSixRows:null};c.prototype.init=function(){if(this.daysOfTheWeek=this.options.daysOfTheWeek||[],!this.options.daysOfTheWeek){this.daysOfTheWeek=[];for(var c=0;7>c;c++)this.daysOfTheWeek.push(b().weekday(c).format("dd").charAt(0))}if(this.options.weekOffset&&(this.daysOfTheWeek=this.shiftWeekdayLabels(this.options.weekOffset)),!a.isFunction(this.options.render)){if(this.options.render=null,"undefined"==typeof _)throw new Error("Underscore was not found. Please include underscore.js OR provide a custom render function.");this.compiledClndrTemplate=_.template(this.options.template)}a(this.element).html("<div class='clndr'></div>"),this.calendarContainer=a(".clndr",this.element),this.bindEvents(),this.render(),this.options.ready&&this.options.ready.apply(this,[])},c.prototype.shiftWeekdayLabels=function(a){for(var b=this.daysOfTheWeek,c=0;a>c;c++)b.push(b.shift());return b},c.prototype.createDaysObject=function(c){daysArray=[];var d=c.startOf("month");if(this.eventsLastMonth=[],this.eventsThisMonth=[],this.eventsNextMonth=[],this.options.events.length)if(this.options.multiDayEvents){if(this.eventsThisMonth=a(this.options.events).filter(function(){return this._clndrStartDateObject.format("YYYY-MM")===c.format("YYYY-MM")||this._clndrEndDateObject.format("YYYY-MM")===c.format("YYYY-MM")?!0:this._clndrStartDateObject.format("YYYY-MM")<=c.format("YYYY-MM")&&this._clndrEndDateObject.format("YYYY-MM")>=c.format("YYYY-MM")?!0:!1}).toArray(),this.options.showAdjacentMonths){var e=c.clone().subtract(1,"months"),f=c.clone().add(1,"months");this.eventsLastMonth=a(this.options.events).filter(function(){return this._clndrStartDateObject.format("YYYY-MM")===e.format("YYYY-MM")||this._clndrEndDateObject.format("YYYY-MM")===e.format("YYYY-MM")?!0:this._clndrStartDateObject.format("YYYY-MM")<=e.format("YYYY-MM")&&this._clndrEndDateObject.format("YYYY-MM")>=e.format("YYYY-MM")?!0:!1}).toArray(),this.eventsNextMonth=a(this.options.events).filter(function(){return this._clndrStartDateObject.format("YYYY-MM")===f.format("YYYY-MM")||this._clndrEndDateObject.format("YYYY-MM")===f.format("YYYY-MM")?!0:this._clndrStartDateObject.format("YYYY-MM")<=f.format("YYYY-MM")&&this._clndrEndDateObject.format("YYYY-MM")>=f.format("YYYY-MM")?!0:!1}).toArray()}}else if(this.eventsThisMonth=a(this.options.events).filter(function(){return this._clndrDateObject.format("YYYY-MM")==c.format("YYYY-MM")}).toArray(),this.options.showAdjacentMonths){var e=c.clone().subtract(1,"months"),f=c.clone().add(1,"months");this.eventsLastMonth=a(this.options.events).filter(function(){return this._clndrDateObject.format("YYYY-MM")==e.format("YYYY-MM")}).toArray(),this.eventsNextMonth=a(this.options.events).filter(function(){return this._clndrDateObject.format("YYYY-MM")==f.format("YYYY-MM")}).toArray()}var g=d.weekday()-this.options.weekOffset;if(0>g&&(g+=7),this.options.showAdjacentMonths)for(var h=0;g>h;h++){var i=b([c.year(),c.month(),h-g+1]);daysArray.push(this.createDayObject(i,this.eventsLastMonth))}else for(var h=0;g>h;h++)daysArray.push(this.calendarDay({classes:this.options.targets.empty+" "+this.options.classes.lastMonth}));for(var j=d.daysInMonth(),h=1;j>=h;h++){var i=b([c.year(),c.month(),h]);daysArray.push(this.createDayObject(i,this.eventsThisMonth))}for(var h=1;daysArray.length%7!==0;){if(this.options.showAdjacentMonths){var i=b([c.year(),c.month(),j+h]);daysArray.push(this.createDayObject(i,this.eventsNextMonth))}else daysArray.push(this.calendarDay({classes:this.options.targets.empty+" "+this.options.classes.nextMonth}));h++}if(this.options.forceSixRows&&42!==daysArray.length)for(var k=b(daysArray[daysArray.length-1].date).add(1,"days");daysArray.length<42;)this.options.showAdjacentMonths?(daysArray.push(this.createDayObject(b(k),this.eventsNextMonth)),k.add(1,"days")):daysArray.push(this.calendarDay({classes:this.options.targets.empty+" "+this.options.classes.nextMonth}));return daysArray},c.prototype.createDayObject=function(a,c){var d=[],e=b(),f=this,g=0,h=c.length;for(g;h>g;g++)if(f.options.multiDayEvents){var i=c[g]._clndrStartDateObject,j=c[g]._clndrEndDateObject;(a.isSame(i,"day")||a.isAfter(i,"day"))&&(a.isSame(j,"day")||a.isBefore(j,"day"))&&d.push(c[g])}else c[g]._clndrDateObject.date()==a.date()&&d.push(c[g]);var k="";return e.format("YYYY-MM-DD")==a.format("YYYY-MM-DD")&&(k+=" "+this.options.classes.today),a.isBefore(e,"day")&&(k+=" "+this.options.classes.past),d.length&&(k+=" "+this.options.classes.event),this.month.month()>a.month()?(k+=" "+this.options.classes.adjacentMonth,k+=this.month.year()===a.year()?" "+this.options.classes.lastMonth:" "+this.options.classes.nextMonth):this.month.month()<a.month()&&(k+=" "+this.options.classes.adjacentMonth,k+=this.month.year()===a.year()?" "+this.options.classes.nextMonth:" "+this.options.classes.lastMonth),this.options.constraints&&(this.options.constraints.startDate&&a.isBefore(b(this.options.constraints.startDate))&&(k+=" "+this.options.classes.inactive),this.options.constraints.endDate&&a.isAfter(b(this.options.constraints.endDate))&&(k+=" "+this.options.classes.inactive)),!a.isValid()&&a.hasOwnProperty("_d")&&void 0!=a._d&&(a=b(a._d)),k+=" calendar-day-"+a.format("YYYY-MM-DD"),k+=" calendar-dow-"+a.weekday(),this.calendarDay({day:a.date(),classes:this.options.targets.day+k,events:d,date:a})},c.prototype.render=function(){this.calendarContainer.children().remove();var a=this.createDaysObject(this.month),c=(this.month,{daysOfTheWeek:this.daysOfTheWeek,numberOfRows:Math.ceil(a.length/7),days:a,month:this.month.format("MMMM"),year:this.month.year(),eventsThisMonth:this.eventsThisMonth,eventsLastMonth:this.eventsLastMonth,eventsNextMonth:this.eventsNextMonth,extras:this.options.extras});if(this.calendarContainer.html(this.options.render?this.options.render.apply(this,[c]):this.compiledClndrTemplate(c)),this.options.constraints){for(var d in this.options.targets)d!=this.options.targets.day&&this.element.find("."+this.options.targets[d]).toggleClass(this.options.classes.inactive,!1);var e=null,f=null;this.options.constraints.startDate&&(e=b(this.options.constraints.startDate)),this.options.constraints.endDate&&(f=b(this.options.constraints.endDate)),e&&this.month.isSame(e,"month")&&this.element.find("."+this.options.targets.previousButton).toggleClass(this.options.classes.inactive,!0),f&&this.month.isSame(f,"month")&&this.element.find("."+this.options.targets.nextButton).toggleClass(this.options.classes.inactive,!0),e&&b(e).subtract(1,"years").isBefore(b(this.month).subtract(1,"years"))&&this.element.find("."+this.options.targets.previousYearButton).toggleClass(this.options.classes.inactive,!0),f&&b(f).add(1,"years").isAfter(b(this.month).add(1,"years"))&&this.element.find("."+this.options.targets.nextYearButton).toggleClass(this.options.classes.inactive,!0),(e&&e.isAfter(b(),"month")||f&&f.isBefore(b(),"month"))&&this.element.find("."+this.options.targets.today).toggleClass(this.options.classes.inactive,!0)}this.options.doneRendering&&this.options.doneRendering.apply(this,[])},c.prototype.bindEvents=function(){var b=a(this.element),c=this;b.on("click","."+this.options.targets.day,function(b){if(c.options.clickEvents.click){var d=c.buildTargetObject(b.currentTarget,!0);c.options.clickEvents.click.apply(c,[d])}c.options.adjacentDaysChangeMonth&&(a(b.currentTarget).is("."+c.options.classes.lastMonth)?c.backActionWithContext(c):a(b.currentTarget).is("."+c.options.classes.nextMonth)&&c.forwardActionWithContext(c))}),b.on("click","."+this.options.targets.empty,function(b){if(c.options.clickEvents.click){var d=c.buildTargetObject(b.currentTarget,!1);c.options.clickEvents.click.apply(c,[d])}c.options.adjacentDaysChangeMonth&&(a(b.currentTarget).is("."+c.options.classes.lastMonth)?c.backActionWithContext(c):a(b.currentTarget).is("."+c.options.classes.nextMonth)&&c.forwardActionWithContext(c))}),b.on("click","."+this.options.targets.previousButton,{context:this},this.backAction).on("click","."+this.options.targets.nextButton,{context:this},this.forwardAction).on("click","."+this.options.targets.todayButton,{context:this},this.todayAction).on("click","."+this.options.targets.nextYearButton,{context:this},this.nextYearAction).on("click","."+this.options.targets.previousYearButton,{context:this},this.previousYearAction)},c.prototype.buildTargetObject=function(c,d){var e={element:c,events:[],date:null};if(d){var f,g=c.className.indexOf("calendar-day-");0!==g?(f=c.className.substring(g+13,g+23),e.date=b(f)):e.date=null,this.options.events&&(e.events=a.makeArray(this.options.multiDayEvents?a(this.options.events).filter(function(){return(e.date.isSame(this._clndrStartDateObject,"day")||e.date.isAfter(this._clndrStartDateObject,"day"))&&(e.date.isSame(this._clndrEndDateObject,"day")||e.date.isBefore(this._clndrEndDateObject,"day"))}):a(this.options.events).filter(function(){return this._clndrDateObject.format("YYYY-MM-DD")==f})))}return e},c.prototype.forwardAction=function(a){var b=a.data.context;b.forwardActionWithContext(b)},c.prototype.backAction=function(a){var b=a.data.context;b.backActionWithContext(b)},c.prototype.backActionWithContext=function(a){if(!a.element.find("."+a.options.targets.previousButton).hasClass("inactive")){var c=!a.month.isSame(b(a.month).subtract(1,"months"),"year");a.month.subtract(1,"months"),a.render(),a.options.clickEvents.previousMonth&&a.options.clickEvents.previousMonth.apply(a,[b(a.month)]),a.options.clickEvents.onMonthChange&&a.options.clickEvents.onMonthChange.apply(a,[b(a.month)]),c&&a.options.clickEvents.onYearChange&&a.options.clickEvents.onYearChange.apply(a,[b(a.month)])}},c.prototype.forwardActionWithContext=function(a){if(!a.element.find("."+a.options.targets.nextButton).hasClass("inactive")){var c=!a.month.isSame(b(a.month).add(1,"months"),"year");a.month.add(1,"months"),a.render(),a.options.clickEvents.nextMonth&&a.options.clickEvents.nextMonth.apply(a,[b(a.month)]),a.options.clickEvents.onMonthChange&&a.options.clickEvents.onMonthChange.apply(a,[b(a.month)]),c&&a.options.clickEvents.onYearChange&&a.options.clickEvents.onYearChange.apply(a,[b(a.month)])}},c.prototype.todayAction=function(a){var c=a.data.context,d=!c.month.isSame(b(),"month"),e=!c.month.isSame(b(),"year");c.month=b().startOf("month"),c.options.clickEvents.today&&c.options.clickEvents.today.apply(c,[b(c.month)]),d&&(c.render(),c.month=b(),c.options.clickEvents.onMonthChange&&c.options.clickEvents.onMonthChange.apply(c,[b(c.month)]),e&&c.options.clickEvents.onYearChange&&c.options.clickEvents.onYearChange.apply(c,[b(c.month)]))},c.prototype.nextYearAction=function(a){var c=a.data.context;c.element.find("."+c.options.targets.nextYearButton).hasClass("inactive")||(c.month.add(1,"years"),c.render(),c.options.clickEvents.nextYear&&c.options.clickEvents.nextYear.apply(c,[b(c.month)]),c.options.clickEvents.onMonthChange&&c.options.clickEvents.onMonthChange.apply(c,[b(c.month)]),c.options.clickEvents.onYearChange&&c.options.clickEvents.onYearChange.apply(c,[b(c.month)]))},c.prototype.previousYearAction=function(a){var c=a.data.context;c.element.find("."+c.options.targets.previousYear).hasClass("inactive")||(c.month.subtract(1,"years"),c.render(),c.options.clickEvents.previousYear&&c.options.clickEvents.previousYear.apply(c,[b(c.month)]),c.options.clickEvents.onMonthChange&&c.options.clickEvents.onMonthChange.apply(c,[b(c.month)]),c.options.clickEvents.onYearChange&&c.options.clickEvents.onYearChange.apply(c,[b(c.month)]))},c.prototype.forward=function(a){return this.month.add(1,"months"),this.render(),a&&a.withCallbacks&&(this.options.clickEvents.onMonthChange&&this.options.clickEvents.onMonthChange.apply(this,[b(this.month)]),0===this.month.month()&&this.options.clickEvents.onYearChange&&this.options.clickEvents.onYearChange.apply(this,[b(this.month)])),this},c.prototype.back=function(a){return this.month.subtract(1,"months"),this.render(),a&&a.withCallbacks&&(this.options.clickEvents.onMonthChange&&this.options.clickEvents.onMonthChange.apply(this,[b(this.month)]),11===this.month.month()&&this.options.clickEvents.onYearChange&&this.options.clickEvents.onYearChange.apply(this,[b(this.month)])),this},c.prototype.next=function(a){return this.forward(a),this},c.prototype.previous=function(a){return this.back(a),this},c.prototype.setMonth=function(a,c){return this.month.month(a),this.render(),c&&c.withCallbacks&&this.options.clickEvents.onMonthChange&&this.options.clickEvents.onMonthChange.apply(this,[b(this.month)]),this},c.prototype.nextYear=function(a){return this.month.add(1,"year"),this.render(),a&&a.withCallbacks&&this.options.clickEvents.onYearChange&&this.options.clickEvents.onYearChange.apply(this,[b(this.month)]),this},c.prototype.previousYear=function(a){return this.month.subtract(1,"year"),this.render(),a&&a.withCallbacks&&this.options.clickEvents.onYearChange&&this.options.clickEvents.onYearChange.apply(this,[b(this.month)]),this},c.prototype.setYear=function(a,c){return this.month.year(a),this.render(),c&&c.withCallbacks&&this.options.clickEvents.onYearChange&&this.options.clickEvents.onYearChange.apply(this,[b(this.month)]),this},c.prototype.setEvents=function(a){return this.options.events=this.options.multiDayEvents?this.addMultiDayMomentObjectsToEvents(a):this.addMomentObjectToEvents(a),this.render(),this},c.prototype.addEvents=function(b){return this.options.events=this.options.multiDayEvents?a.merge(this.options.events,this.addMultiDayMomentObjectsToEvents(b)):a.merge(this.options.events,this.addMomentObjectToEvents(b)),this.render(),this},c.prototype.removeEvents=function(a){for(var b=this.options.events.length-1;b>=0;b--)1==a(this.options.events[b])&&this.options.events.splice(b,1);return this.render(),this},c.prototype.addMomentObjectToEvents=function(a){var c=this,d=0,e=a.length;for(d;e>d;d++)a[d]._clndrDateObject=b(a[d][c.options.dateParameter]);return a},c.prototype.addMultiDayMomentObjectsToEvents=function(a){var c=this,d=0,e=a.length;for(d;e>d;d++)a[d][c.options.multiDayEvents.endDate]||a[d][c.options.multiDayEvents.startDate]?(a[d]._clndrEndDateObject=b(a[d][c.options.multiDayEvents.endDate]||a[d][c.options.multiDayEvents.startDate]),a[d]._clndrStartDateObject=b(a[d][c.options.multiDayEvents.startDate]||a[d][c.options.multiDayEvents.endDate])):(a[d]._clndrEndDateObject=b(a[d][c.options.multiDayEvents.singleDay]),a[d]._clndrStartDateObject=b(a[d][c.options.multiDayEvents.singleDay]));return a},c.prototype.calendarDay=function(b){var c={day:"",classes:this.options.targets.empty,events:[],date:null};return a.extend({},c,b)},a.fn.clndr=function(a){if(1===this.length){if(!this.data("plugin_clndr")){var b=new c(this,a);return this.data("plugin_clndr",b),b}}else if(this.length>1)throw new Error("CLNDR does not support multiple elements yet. Make sure your clndr selector returns only one element.")}});

// GMaps.js v0.4.16
!function(e,t){"object"==typeof exports?module.exports=t():"function"==typeof define&&define.amd&&define("GMaps",[],t),e.GMaps=t()}(this,function(){if("object"!=typeof window.google||!window.google.maps)throw"Google Maps API is required. Please register the following JavaScript library http://maps.google.com/maps/api/js?sensor=true.";var t=function(e,t){var o;if(e===t)return e;for(o in t)e[o]=t[o];return e},o=function(e,t){var o,n=Array.prototype.slice.call(arguments,2),r=[],s=e.length;if(Array.prototype.map&&e.map===Array.prototype.map)r=Array.prototype.map.call(e,function(e){return callback_params=n,callback_params.splice(0,0,e),t.apply(this,callback_params)});else for(o=0;s>o;o++)callback_params=n,callback_params.splice(0,0,e[o]),r.push(t.apply(this,callback_params));return r},n=function(e){var t,o=[];for(t=0;t<e.length;t++)o=o.concat(e[t]);return o},r=function(e,t){var o=e[0],n=e[1];return t&&(o=e[1],n=e[0]),new google.maps.LatLng(o,n)},s=function(e,t){var o;for(o=0;o<e.length;o++)e[o]instanceof google.maps.LatLng||(e[o]=e[o].length>0&&"object"==typeof e[o][0]?s(e[o],t):r(e[o],t));return e},i=function(e,t){var o,e=e.replace("#","");return o="jQuery"in this&&t?$("#"+e,t)[0]:document.getElementById(e)},a=function(e){var t=0,o=0;if(e.offsetParent)do t+=e.offsetLeft,o+=e.offsetTop;while(e=e.offsetParent);return[t,o]},l=function(){"use strict";var e=document,o=function(n){if(!this)return new o(n);n.zoom=n.zoom||15,n.mapType=n.mapType||"roadmap";var r,s=this,l=["bounds_changed","center_changed","click","dblclick","drag","dragend","dragstart","idle","maptypeid_changed","projection_changed","resize","tilesloaded","zoom_changed"],p=["mousemove","mouseout","mouseover"],g=["el","lat","lng","mapType","width","height","markerClusterer","enableNewStyle"],c=n.el||n.div,h=n.markerClusterer,u=google.maps.MapTypeId[n.mapType.toUpperCase()],d=new google.maps.LatLng(n.lat,n.lng),m=n.zoomControl||!0,f=n.zoomControlOpt||{style:"DEFAULT",position:"TOP_LEFT"},y=f.style||"DEFAULT",v=f.position||"TOP_LEFT",k=n.panControl||!0,w=n.mapTypeControl||!0,L=n.scaleControl||!0,b=n.streetViewControl||!0,_=_||!0,M={},x={zoom:this.zoom,center:d,mapTypeId:u},C={panControl:k,zoomControl:m,zoomControlOptions:{style:google.maps.ZoomControlStyle[y],position:google.maps.ControlPosition[v]},mapTypeControl:w,scaleControl:L,streetViewControl:b,overviewMapControl:_};if(this.el="string"==typeof n.el||"string"==typeof n.div?i(c,n.context):c,"undefined"==typeof this.el||null===this.el)throw"No element defined.";for(window.context_menu=window.context_menu||{},window.context_menu[s.el.id]={},this.controls=[],this.overlays=[],this.layers=[],this.singleLayers={},this.markers=[],this.polylines=[],this.routes=[],this.polygons=[],this.infoWindow=null,this.overlay_el=null,this.zoom=n.zoom,this.registered_events={},this.el.style.width=n.width||this.el.scrollWidth||this.el.offsetWidth,this.el.style.height=n.height||this.el.scrollHeight||this.el.offsetHeight,google.maps.visualRefresh=n.enableNewStyle,r=0;r<g.length;r++)delete n[g[r]];for(1!=n.disableDefaultUI&&(x=t(x,C)),M=t(x,n),r=0;r<l.length;r++)delete M[l[r]];for(r=0;r<p.length;r++)delete M[p[r]];this.map=new google.maps.Map(this.el,M),h&&(this.markerClusterer=h.apply(this,[this.map]));var P=function(e,t){var o="",n=window.context_menu[s.el.id][e];for(var r in n)if(n.hasOwnProperty(r)){var l=n[r];o+='<li><a id="'+e+"_"+r+'" href="#">'+l.title+"</a></li>"}if(i("gmaps_context_menu")){var p=i("gmaps_context_menu");p.innerHTML=o;var r,g=p.getElementsByTagName("a"),c=g.length;for(r=0;c>r;r++){var h=g[r],u=function(o){o.preventDefault(),n[this.id.replace(e+"_","")].action.apply(s,[t]),s.hideContextMenu()};google.maps.event.clearListeners(h,"click"),google.maps.event.addDomListenerOnce(h,"click",u,!1)}var d=a.apply(this,[s.el]),m=d[0]+t.pixel.x-15,f=d[1]+t.pixel.y-15;p.style.left=m+"px",p.style.top=f+"px",p.style.display="block"}};this.buildContextMenu=function(e,t){if("marker"===e){t.pixel={};var o=new google.maps.OverlayView;o.setMap(s.map),o.draw=function(){var n=o.getProjection(),r=t.marker.getPosition();t.pixel=n.fromLatLngToContainerPixel(r),P(e,t)}}else P(e,t)},this.setContextMenu=function(t){window.context_menu[s.el.id][t.control]={};var o,n=e.createElement("ul");for(o in t.options)if(t.options.hasOwnProperty(o)){var r=t.options[o];window.context_menu[s.el.id][t.control][r.name]={title:r.title,action:r.action}}n.id="gmaps_context_menu",n.style.display="none",n.style.position="absolute",n.style.minWidth="100px",n.style.background="white",n.style.listStyle="none",n.style.padding="8px",n.style.boxShadow="2px 2px 6px #ccc",e.body.appendChild(n);var a=i("gmaps_context_menu");google.maps.event.addDomListener(a,"mouseout",function(e){e.relatedTarget&&this.contains(e.relatedTarget)||window.setTimeout(function(){a.style.display="none"},400)},!1)},this.hideContextMenu=function(){var e=i("gmaps_context_menu");e&&(e.style.display="none")};var O=function(e,t){google.maps.event.addListener(e,t,function(e){void 0==e&&(e=this),n[t].apply(this,[e]),s.hideContextMenu()})};google.maps.event.addListener(this.map,"zoom_changed",this.hideContextMenu);for(var T=0;T<l.length;T++){var z=l[T];z in n&&O(this.map,z)}for(var T=0;T<p.length;T++){var z=p[T];z in n&&O(this.map,z)}google.maps.event.addListener(this.map,"rightclick",function(e){n.rightclick&&n.rightclick.apply(this,[e]),void 0!=window.context_menu[s.el.id].map&&s.buildContextMenu("map",e)}),this.refresh=function(){google.maps.event.trigger(this.map,"resize")},this.fitZoom=function(){var e,t=[],o=this.markers.length;for(e=0;o>e;e++)"boolean"==typeof this.markers[e].visible&&this.markers[e].visible&&t.push(this.markers[e].getPosition());this.fitLatLngBounds(t)},this.fitLatLngBounds=function(e){for(var t=e.length,o=new google.maps.LatLngBounds,n=0;t>n;n++)o.extend(e[n]);this.map.fitBounds(o)},this.setCenter=function(e,t,o){this.map.panTo(new google.maps.LatLng(e,t)),o&&o()},this.getElement=function(){return this.el},this.zoomIn=function(e){e=e||1,this.zoom=this.map.getZoom()+e,this.map.setZoom(this.zoom)},this.zoomOut=function(e){e=e||1,this.zoom=this.map.getZoom()-e,this.map.setZoom(this.zoom)};var S,W=[];for(S in this.map)"function"!=typeof this.map[S]||this[S]||W.push(S);for(r=0;r<W.length;r++)!function(e,t,o){e[o]=function(){return t[o].apply(t,arguments)}}(this,this.map,W[r])};return o}(this);l.prototype.createControl=function(e){var t=document.createElement("div");t.style.cursor="pointer",e.disableDefaultStyles!==!0&&(t.style.fontFamily="Roboto, Arial, sans-serif",t.style.fontSize="11px",t.style.boxShadow="rgba(0, 0, 0, 0.298039) 0px 1px 4px -1px");for(var o in e.style)t.style[o]=e.style[o];e.id&&(t.id=e.id),e.classes&&(t.className=e.classes),e.content&&("string"==typeof e.content?t.innerHTML=e.content:e.content instanceof HTMLElement&&t.appendChild(e.content)),e.position&&(t.position=google.maps.ControlPosition[e.position.toUpperCase()]);for(var n in e.events)!function(t,o){google.maps.event.addDomListener(t,o,function(){e.events[o].apply(this,[this])})}(t,n);return t.index=1,t},l.prototype.addControl=function(e){var t=this.createControl(e);return this.controls.push(t),this.map.controls[t.position].push(t),t},l.prototype.removeControl=function(e){for(var t=null,o=0;o<this.controls.length;o++)this.controls[o]==e&&(t=this.controls[o].position,this.controls.splice(o,1));if(t)for(o=0;o<this.map.controls.length;o++){var n=this.map.controls[e.position];if(n.getAt(o)==e){n.removeAt(o);break}}return e},l.prototype.createMarker=function(e){if(void 0==e.lat&&void 0==e.lng&&void 0==e.position)throw"No latitude or longitude defined.";var o=this,n=e.details,r=e.fences,s=e.outside,i={position:new google.maps.LatLng(e.lat,e.lng),map:null},a=t(i,e);delete a.lat,delete a.lng,delete a.fences,delete a.outside;var l=new google.maps.Marker(a);if(l.fences=r,e.infoWindow){l.infoWindow=new google.maps.InfoWindow(e.infoWindow);for(var p=["closeclick","content_changed","domready","position_changed","zindex_changed"],g=0;g<p.length;g++)!function(t,o){e.infoWindow[o]&&google.maps.event.addListener(t,o,function(t){e.infoWindow[o].apply(this,[t])})}(l.infoWindow,p[g])}for(var c=["animation_changed","clickable_changed","cursor_changed","draggable_changed","flat_changed","icon_changed","position_changed","shadow_changed","shape_changed","title_changed","visible_changed","zindex_changed"],h=["dblclick","drag","dragend","dragstart","mousedown","mouseout","mouseover","mouseup"],g=0;g<c.length;g++)!function(t,o){e[o]&&google.maps.event.addListener(t,o,function(){e[o].apply(this,[this])})}(l,c[g]);for(var g=0;g<h.length;g++)!function(t,o,n){e[n]&&google.maps.event.addListener(o,n,function(o){o.pixel||(o.pixel=t.getProjection().fromLatLngToPoint(o.latLng)),e[n].apply(this,[o])})}(this.map,l,h[g]);return google.maps.event.addListener(l,"click",function(){this.details=n,e.click&&e.click.apply(this,[this]),l.infoWindow&&(o.hideInfoWindows(),l.infoWindow.open(o.map,l))}),google.maps.event.addListener(l,"rightclick",function(t){t.marker=this,e.rightclick&&e.rightclick.apply(this,[t]),void 0!=window.context_menu[o.el.id].marker&&o.buildContextMenu("marker",t)}),l.fences&&google.maps.event.addListener(l,"dragend",function(){o.checkMarkerGeofence(l,function(e,t){s(e,t)})}),l},l.prototype.addMarker=function(e){var t;if(e.hasOwnProperty("gm_accessors_"))t=e;else{if(!(e.hasOwnProperty("lat")&&e.hasOwnProperty("lng")||e.position))throw"No latitude or longitude defined.";t=this.createMarker(e)}return t.setMap(this.map),this.markerClusterer&&this.markerClusterer.addMarker(t),this.markers.push(t),l.fire("marker_added",t,this),t},l.prototype.addMarkers=function(e){for(var t,o=0;t=e[o];o++)this.addMarker(t);return this.markers},l.prototype.hideInfoWindows=function(){for(var e,t=0;e=this.markers[t];t++)e.infoWindow&&e.infoWindow.close()},l.prototype.removeMarker=function(e){for(var t=0;t<this.markers.length;t++)if(this.markers[t]===e){this.markers[t].setMap(null),this.markers.splice(t,1),this.markerClusterer&&this.markerClusterer.removeMarker(e),l.fire("marker_removed",e,this);break}return e},l.prototype.removeMarkers=function(e){var t=[];if("undefined"==typeof e){for(var o=0;o<this.markers.length;o++){var n=this.markers[o];n.setMap(null),this.markerClusterer&&this.markerClusterer.removeMarker(n),l.fire("marker_removed",n,this)}this.markers=t}else{for(var o=0;o<e.length;o++){var r=this.markers.indexOf(e[o]);if(r>-1){var n=this.markers[r];n.setMap(null),this.markerClusterer&&this.markerClusterer.removeMarker(n),l.fire("marker_removed",n,this)}}for(var o=0;o<this.markers.length;o++){var n=this.markers[o];null!=n.getMap()&&t.push(n)}this.markers=t}},l.prototype.drawOverlay=function(e){var t=new google.maps.OverlayView,o=!0;return t.setMap(this.map),null!=e.auto_show&&(o=e.auto_show),t.onAdd=function(){var o=document.createElement("div");o.style.borderStyle="none",o.style.borderWidth="0px",o.style.position="absolute",o.style.zIndex=100,o.innerHTML=e.content,t.el=o,e.layer||(e.layer="overlayLayer");var n=this.getPanes(),r=n[e.layer],s=["contextmenu","DOMMouseScroll","dblclick","mousedown"];r.appendChild(o);for(var i=0;i<s.length;i++)!function(e,t){google.maps.event.addDomListener(e,t,function(e){-1!=navigator.userAgent.toLowerCase().indexOf("msie")&&document.all?(e.cancelBubble=!0,e.returnValue=!1):e.stopPropagation()})}(o,s[i]);e.click&&(n.overlayMouseTarget.appendChild(t.el),google.maps.event.addDomListener(t.el,"click",function(){e.click.apply(t,[t])})),google.maps.event.trigger(this,"ready")},t.draw=function(){var n=this.getProjection(),r=n.fromLatLngToDivPixel(new google.maps.LatLng(e.lat,e.lng));e.horizontalOffset=e.horizontalOffset||0,e.verticalOffset=e.verticalOffset||0;var s=t.el,i=s.children[0],a=i.clientHeight,l=i.clientWidth;switch(e.verticalAlign){case"top":s.style.top=r.y-a+e.verticalOffset+"px";break;default:case"middle":s.style.top=r.y-a/2+e.verticalOffset+"px";break;case"bottom":s.style.top=r.y+e.verticalOffset+"px"}switch(e.horizontalAlign){case"left":s.style.left=r.x-l+e.horizontalOffset+"px";break;default:case"center":s.style.left=r.x-l/2+e.horizontalOffset+"px";break;case"right":s.style.left=r.x+e.horizontalOffset+"px"}s.style.display=o?"block":"none",o||e.show.apply(this,[s])},t.onRemove=function(){var o=t.el;e.remove?e.remove.apply(this,[o]):(t.el.parentNode.removeChild(t.el),t.el=null)},this.overlays.push(t),t},l.prototype.removeOverlay=function(e){for(var t=0;t<this.overlays.length;t++)if(this.overlays[t]===e){this.overlays[t].setMap(null),this.overlays.splice(t,1);break}},l.prototype.removeOverlays=function(){for(var e,t=0;e=this.overlays[t];t++)e.setMap(null);this.overlays=[]},l.prototype.drawPolyline=function(e){var t=[],o=e.path;if(o.length)if(void 0===o[0][0])t=o;else for(var n,r=0;n=o[r];r++)t.push(new google.maps.LatLng(n[0],n[1]));var s={map:this.map,path:t,strokeColor:e.strokeColor,strokeOpacity:e.strokeOpacity,strokeWeight:e.strokeWeight,geodesic:e.geodesic,clickable:!0,editable:!1,visible:!0};e.hasOwnProperty("clickable")&&(s.clickable=e.clickable),e.hasOwnProperty("editable")&&(s.editable=e.editable),e.hasOwnProperty("icons")&&(s.icons=e.icons),e.hasOwnProperty("zIndex")&&(s.zIndex=e.zIndex);for(var i=new google.maps.Polyline(s),a=["click","dblclick","mousedown","mousemove","mouseout","mouseover","mouseup","rightclick"],p=0;p<a.length;p++)!function(t,o){e[o]&&google.maps.event.addListener(t,o,function(t){e[o].apply(this,[t])})}(i,a[p]);return this.polylines.push(i),l.fire("polyline_added",i,this),i},l.prototype.removePolyline=function(e){for(var t=0;t<this.polylines.length;t++)if(this.polylines[t]===e){this.polylines[t].setMap(null),this.polylines.splice(t,1),l.fire("polyline_removed",e,this);break}},l.prototype.removePolylines=function(){for(var e,t=0;e=this.polylines[t];t++)e.setMap(null);this.polylines=[]},l.prototype.drawCircle=function(e){e=t({map:this.map,center:new google.maps.LatLng(e.lat,e.lng)},e),delete e.lat,delete e.lng;for(var o=new google.maps.Circle(e),n=["click","dblclick","mousedown","mousemove","mouseout","mouseover","mouseup","rightclick"],r=0;r<n.length;r++)!function(t,o){e[o]&&google.maps.event.addListener(t,o,function(t){e[o].apply(this,[t])})}(o,n[r]);return this.polygons.push(o),o},l.prototype.drawRectangle=function(e){e=t({map:this.map},e);var o=new google.maps.LatLngBounds(new google.maps.LatLng(e.bounds[0][0],e.bounds[0][1]),new google.maps.LatLng(e.bounds[1][0],e.bounds[1][1]));e.bounds=o;for(var n=new google.maps.Rectangle(e),r=["click","dblclick","mousedown","mousemove","mouseout","mouseover","mouseup","rightclick"],s=0;s<r.length;s++)!function(t,o){e[o]&&google.maps.event.addListener(t,o,function(t){e[o].apply(this,[t])})}(n,r[s]);return this.polygons.push(n),n},l.prototype.drawPolygon=function(e){var r=!1;e.hasOwnProperty("useGeoJSON")&&(r=e.useGeoJSON),delete e.useGeoJSON,e=t({map:this.map},e),0==r&&(e.paths=[e.paths.slice(0)]),e.paths.length>0&&e.paths[0].length>0&&(e.paths=n(o(e.paths,s,r)));for(var i=new google.maps.Polygon(e),a=["click","dblclick","mousedown","mousemove","mouseout","mouseover","mouseup","rightclick"],p=0;p<a.length;p++)!function(t,o){e[o]&&google.maps.event.addListener(t,o,function(t){e[o].apply(this,[t])})}(i,a[p]);return this.polygons.push(i),l.fire("polygon_added",i,this),i},l.prototype.removePolygon=function(e){for(var t=0;t<this.polygons.length;t++)if(this.polygons[t]===e){this.polygons[t].setMap(null),this.polygons.splice(t,1),l.fire("polygon_removed",e,this);break}},l.prototype.removePolygons=function(){for(var e,t=0;e=this.polygons[t];t++)e.setMap(null);this.polygons=[]},l.prototype.getFromFusionTables=function(e){var t=e.events;delete e.events;var o=e,n=new google.maps.FusionTablesLayer(o);for(var r in t)!function(e,o){google.maps.event.addListener(e,o,function(e){t[o].apply(this,[e])})}(n,r);return this.layers.push(n),n},l.prototype.loadFromFusionTables=function(e){var t=this.getFromFusionTables(e);return t.setMap(this.map),t},l.prototype.getFromKML=function(e){var t=e.url,o=e.events;delete e.url,delete e.events;var n=e,r=new google.maps.KmlLayer(t,n);for(var s in o)!function(e,t){google.maps.event.addListener(e,t,function(e){o[t].apply(this,[e])})}(r,s);return this.layers.push(r),r},l.prototype.loadFromKML=function(e){var t=this.getFromKML(e);return t.setMap(this.map),t},l.prototype.addLayer=function(e,t){t=t||{};var o;switch(e){case"weather":this.singleLayers.weather=o=new google.maps.weather.WeatherLayer;break;case"clouds":this.singleLayers.clouds=o=new google.maps.weather.CloudLayer;break;case"traffic":this.singleLayers.traffic=o=new google.maps.TrafficLayer;break;case"transit":this.singleLayers.transit=o=new google.maps.TransitLayer;break;case"bicycling":this.singleLayers.bicycling=o=new google.maps.BicyclingLayer;break;case"panoramio":this.singleLayers.panoramio=o=new google.maps.panoramio.PanoramioLayer,o.setTag(t.filter),delete t.filter,t.click&&google.maps.event.addListener(o,"click",function(e){t.click(e),delete t.click});break;case"places":if(this.singleLayers.places=o=new google.maps.places.PlacesService(this.map),t.search||t.nearbySearch||t.radarSearch){var n={bounds:t.bounds||null,keyword:t.keyword||null,location:t.location||null,name:t.name||null,radius:t.radius||null,rankBy:t.rankBy||null,types:t.types||null};t.radarSearch&&o.radarSearch(n,t.radarSearch),t.search&&o.search(n,t.search),t.nearbySearch&&o.nearbySearch(n,t.nearbySearch)}if(t.textSearch){var r={bounds:t.bounds||null,location:t.location||null,query:t.query||null,radius:t.radius||null};o.textSearch(r,t.textSearch)}}return void 0!==o?("function"==typeof o.setOptions&&o.setOptions(t),"function"==typeof o.setMap&&o.setMap(this.map),o):void 0},l.prototype.removeLayer=function(e){if("string"==typeof e&&void 0!==this.singleLayers[e])this.singleLayers[e].setMap(null),delete this.singleLayers[e];else for(var t=0;t<this.layers.length;t++)if(this.layers[t]===e){this.layers[t].setMap(null),this.layers.splice(t,1);break}};var p,g;return l.prototype.getRoutes=function(e){switch(e.travelMode){case"bicycling":p=google.maps.TravelMode.BICYCLING;break;case"transit":p=google.maps.TravelMode.TRANSIT;break;case"driving":p=google.maps.TravelMode.DRIVING;break;default:p=google.maps.TravelMode.WALKING}g="imperial"===e.unitSystem?google.maps.UnitSystem.IMPERIAL:google.maps.UnitSystem.METRIC;var o={avoidHighways:!1,avoidTolls:!1,optimizeWaypoints:!1,waypoints:[]},n=t(o,e);n.origin=/string/.test(typeof e.origin)?e.origin:new google.maps.LatLng(e.origin[0],e.origin[1]),n.destination=/string/.test(typeof e.destination)?e.destination:new google.maps.LatLng(e.destination[0],e.destination[1]),n.travelMode=p,n.unitSystem=g,delete n.callback,delete n.error;var r=this,s=new google.maps.DirectionsService;s.route(n,function(t,o){if(o===google.maps.DirectionsStatus.OK){for(var n in t.routes)t.routes.hasOwnProperty(n)&&r.routes.push(t.routes[n]);e.callback&&e.callback(r.routes)}else e.error&&e.error(t,o)})},l.prototype.removeRoutes=function(){this.routes=[]},l.prototype.getElevations=function(e){e=t({locations:[],path:!1,samples:256},e),e.locations.length>0&&e.locations[0].length>0&&(e.locations=n(o([e.locations],s,!1)));var r=e.callback;delete e.callback;var i=new google.maps.ElevationService;if(e.path){var a={path:e.locations,samples:e.samples};i.getElevationAlongPath(a,function(e,t){r&&"function"==typeof r&&r(e,t)})}else delete e.path,delete e.samples,i.getElevationForLocations(e,function(e,t){r&&"function"==typeof r&&r(e,t)})},l.prototype.cleanRoute=l.prototype.removePolylines,l.prototype.drawRoute=function(e){var t=this;this.getRoutes({origin:e.origin,destination:e.destination,travelMode:e.travelMode,waypoints:e.waypoints,unitSystem:e.unitSystem,error:e.error,callback:function(o){o.length>0&&(t.drawPolyline({path:o[o.length-1].overview_path,strokeColor:e.strokeColor,strokeOpacity:e.strokeOpacity,strokeWeight:e.strokeWeight}),e.callback&&e.callback(o[o.length-1]))}})},l.prototype.travelRoute=function(e){if(e.origin&&e.destination)this.getRoutes({origin:e.origin,destination:e.destination,travelMode:e.travelMode,waypoints:e.waypoints,unitSystem:e.unitSystem,error:e.error,callback:function(t){if(t.length>0&&e.start&&e.start(t[t.length-1]),t.length>0&&e.step){var o=t[t.length-1];if(o.legs.length>0)for(var n,r=o.legs[0].steps,s=0;n=r[s];s++)n.step_number=s,e.step(n,o.legs[0].steps.length-1)}t.length>0&&e.end&&e.end(t[t.length-1])}});else if(e.route&&e.route.legs.length>0)for(var t,o=e.route.legs[0].steps,n=0;t=o[n];n++)t.step_number=n,e.step(t)},l.prototype.drawSteppedRoute=function(e){var t=this;if(e.origin&&e.destination)this.getRoutes({origin:e.origin,destination:e.destination,travelMode:e.travelMode,waypoints:e.waypoints,error:e.error,callback:function(o){if(o.length>0&&e.start&&e.start(o[o.length-1]),o.length>0&&e.step){var n=o[o.length-1];if(n.legs.length>0)for(var r,s=n.legs[0].steps,i=0;r=s[i];i++)r.step_number=i,t.drawPolyline({path:r.path,strokeColor:e.strokeColor,strokeOpacity:e.strokeOpacity,strokeWeight:e.strokeWeight}),e.step(r,n.legs[0].steps.length-1)}o.length>0&&e.end&&e.end(o[o.length-1])}});else if(e.route&&e.route.legs.length>0)for(var o,n=e.route.legs[0].steps,r=0;o=n[r];r++)o.step_number=r,t.drawPolyline({path:o.path,strokeColor:e.strokeColor,strokeOpacity:e.strokeOpacity,strokeWeight:e.strokeWeight}),e.step(o)},l.Route=function(e){this.origin=e.origin,this.destination=e.destination,this.waypoints=e.waypoints,this.map=e.map,this.route=e.route,this.step_count=0,this.steps=this.route.legs[0].steps,this.steps_length=this.steps.length,this.polyline=this.map.drawPolyline({path:new google.maps.MVCArray,strokeColor:e.strokeColor,strokeOpacity:e.strokeOpacity,strokeWeight:e.strokeWeight}).getPath()},l.Route.prototype.getRoute=function(t){var o=this;this.map.getRoutes({origin:this.origin,destination:this.destination,travelMode:t.travelMode,waypoints:this.waypoints||[],error:t.error,callback:function(){o.route=e[0],t.callback&&t.callback.call(o)}})},l.Route.prototype.back=function(){if(this.step_count>0){this.step_count--;var e=this.route.legs[0].steps[this.step_count].path;for(var t in e)e.hasOwnProperty(t)&&this.polyline.pop()}},l.Route.prototype.forward=function(){if(this.step_count<this.steps_length){var e=this.route.legs[0].steps[this.step_count].path;for(var t in e)e.hasOwnProperty(t)&&this.polyline.push(e[t]);this.step_count++}},l.prototype.checkGeofence=function(e,t,o){return o.containsLatLng(new google.maps.LatLng(e,t))},l.prototype.checkMarkerGeofence=function(e,t){if(e.fences)for(var o,n=0;o=e.fences[n];n++){var r=e.getPosition();this.checkGeofence(r.lat(),r.lng(),o)||t(e,o)}},l.prototype.toImage=function(e){var e=e||{},t={};if(t.size=e.size||[this.el.clientWidth,this.el.clientHeight],t.lat=this.getCenter().lat(),t.lng=this.getCenter().lng(),this.markers.length>0){t.markers=[];for(var o=0;o<this.markers.length;o++)t.markers.push({lat:this.markers[o].getPosition().lat(),lng:this.markers[o].getPosition().lng()})}if(this.polylines.length>0){var n=this.polylines[0];t.polyline={},t.polyline.path=google.maps.geometry.encoding.encodePath(n.getPath()),t.polyline.strokeColor=n.strokeColor,t.polyline.strokeOpacity=n.strokeOpacity,t.polyline.strokeWeight=n.strokeWeight}return l.staticMapURL(t)},l.staticMapURL=function(e){function t(e,t){if("#"===e[0]&&(e=e.replace("#","0x"),t)){if(t=parseFloat(t),t=Math.min(1,Math.max(t,0)),0===t)return"0x00000000";t=(255*t).toString(16),1===t.length&&(t+=t),e=e.slice(0,8)+t}return e}var o,n=[],r="http://maps.googleapis.com/maps/api/staticmap";e.url&&(r=e.url,delete e.url),r+="?";var s=e.markers;delete e.markers,!s&&e.marker&&(s=[e.marker],delete e.marker);var i=e.styles;delete e.styles;var a=e.polyline;if(delete e.polyline,e.center)n.push("center="+e.center),delete e.center;else if(e.address)n.push("center="+e.address),delete e.address;else if(e.lat)n.push(["center=",e.lat,",",e.lng].join("")),delete e.lat,delete e.lng;else if(e.visible){var l=encodeURI(e.visible.join("|"));n.push("visible="+l)}var p=e.size;p?(p.join&&(p=p.join("x")),delete e.size):p="630x300",n.push("size="+p),e.zoom||e.zoom===!1||(e.zoom=15);var g=e.hasOwnProperty("sensor")?!!e.sensor:!0;delete e.sensor,n.push("sensor="+g);for(var c in e)e.hasOwnProperty(c)&&n.push(c+"="+e[c]);if(s)for(var h,u,d=0;o=s[d];d++){h=[],o.size&&"normal"!==o.size?(h.push("size:"+o.size),delete o.size):o.icon&&(h.push("icon:"+encodeURI(o.icon)),delete o.icon),o.color&&(h.push("color:"+o.color.replace("#","0x")),delete o.color),o.label&&(h.push("label:"+o.label[0].toUpperCase()),delete o.label),u=o.address?o.address:o.lat+","+o.lng,delete o.address,delete o.lat,delete o.lng;for(var c in o)o.hasOwnProperty(c)&&h.push(c+":"+o[c]);h.length||0===d?(h.push(u),h=h.join("|"),n.push("markers="+encodeURI(h))):(h=n.pop()+encodeURI("|"+u),n.push(h))}if(i)for(var d=0;d<i.length;d++){var m=[];i[d].featureType&&m.push("feature:"+i[d].featureType.toLowerCase()),i[d].elementType&&m.push("element:"+i[d].elementType.toLowerCase());for(var f=0;f<i[d].stylers.length;f++)for(var y in i[d].stylers[f]){var v=i[d].stylers[f][y];("hue"==y||"color"==y)&&(v="0x"+v.substring(1)),m.push(y+":"+v)}var k=m.join("|");""!=k&&n.push("style="+k)}if(a){if(o=a,a=[],o.strokeWeight&&a.push("weight:"+parseInt(o.strokeWeight,10)),o.strokeColor){var w=t(o.strokeColor,o.strokeOpacity);a.push("color:"+w)}if(o.fillColor){var L=t(o.fillColor,o.fillOpacity);a.push("fillcolor:"+L)}var b=o.path;if(b.join)for(var _,f=0;_=b[f];f++)a.push(_.join(","));else a.push("enc:"+b);a=a.join("|"),n.push("path="+encodeURI(a))}var M=window.devicePixelRatio||1;return n.push("scale="+M),n=n.join("&"),r+n},l.prototype.addMapType=function(e,t){if(!t.hasOwnProperty("getTileUrl")||"function"!=typeof t.getTileUrl)throw"'getTileUrl' function required.";t.tileSize=t.tileSize||new google.maps.Size(256,256);var o=new google.maps.ImageMapType(t);this.map.mapTypes.set(e,o)},l.prototype.addOverlayMapType=function(e){if(!e.hasOwnProperty("getTile")||"function"!=typeof e.getTile)throw"'getTile' function required.";var t=e.index;delete e.index,this.map.overlayMapTypes.insertAt(t,e)},l.prototype.removeOverlayMapType=function(e){this.map.overlayMapTypes.removeAt(e)},l.prototype.addStyle=function(e){var t=new google.maps.StyledMapType(e.styles,{name:e.styledMapName});this.map.mapTypes.set(e.mapTypeId,t)},l.prototype.setStyle=function(e){this.map.setMapTypeId(e)},l.prototype.createPanorama=function(e){return e.hasOwnProperty("lat")&&e.hasOwnProperty("lng")||(e.lat=this.getCenter().lat(),e.lng=this.getCenter().lng()),this.panorama=l.createPanorama(e),this.map.setStreetView(this.panorama),this.panorama},l.createPanorama=function(e){var o=i(e.el,e.context);e.position=new google.maps.LatLng(e.lat,e.lng),delete e.el,delete e.context,delete e.lat,delete e.lng;for(var n=["closeclick","links_changed","pano_changed","position_changed","pov_changed","resize","visible_changed"],r=t({visible:!0},e),s=0;s<n.length;s++)delete r[n[s]];for(var a=new google.maps.StreetViewPanorama(o,r),s=0;s<n.length;s++)!function(t,o){e[o]&&google.maps.event.addListener(t,o,function(){e[o].apply(this)})}(a,n[s]);return a},l.prototype.on=function(e,t){return l.on(e,this,t)},l.prototype.off=function(e){l.off(e,this)},l.custom_events=["marker_added","marker_removed","polyline_added","polyline_removed","polygon_added","polygon_removed","geolocated","geolocation_failed"],l.on=function(e,t,o){if(-1==l.custom_events.indexOf(e))return t instanceof l&&(t=t.map),google.maps.event.addListener(t,e,o);var n={handler:o,eventName:e};return t.registered_events[e]=t.registered_events[e]||[],t.registered_events[e].push(n),n},l.off=function(e,t){-1==l.custom_events.indexOf(e)?(t instanceof l&&(t=t.map),google.maps.event.clearListeners(t,e)):t.registered_events[e]=[]},l.fire=function(e,t,o){if(-1==l.custom_events.indexOf(e))google.maps.event.trigger(t,e,Array.prototype.slice.apply(arguments).slice(2));else if(e in o.registered_events)for(var n=o.registered_events[e],r=0;r<n.length;r++)!function(e,t,o){e.apply(t,[o])}(n[r].handler,o,t)},l.geolocate=function(e){var t=e.always||e.complete;navigator.geolocation?navigator.geolocation.getCurrentPosition(function(o){e.success(o),t&&t()},function(o){e.error(o),t&&t()},e.options):(e.not_supported(),t&&t())},l.geocode=function(e){this.geocoder=new google.maps.Geocoder;var t=e.callback;e.hasOwnProperty("lat")&&e.hasOwnProperty("lng")&&(e.latLng=new google.maps.LatLng(e.lat,e.lng)),delete e.lat,delete e.lng,delete e.callback,this.geocoder.geocode(e,function(e,o){t(e,o)})},google.maps.Polygon.prototype.getBounds||(google.maps.Polygon.prototype.getBounds=function(){for(var e,t=new google.maps.LatLngBounds,o=this.getPaths(),n=0;n<o.getLength();n++){e=o.getAt(n);for(var r=0;r<e.getLength();r++)t.extend(e.getAt(r))}return t}),google.maps.Polygon.prototype.containsLatLng||(google.maps.Polygon.prototype.containsLatLng=function(e){var t=this.getBounds();if(null!==t&&!t.contains(e))return!1;for(var o=!1,n=this.getPaths().getLength(),r=0;n>r;r++)for(var s=this.getPaths().getAt(r),i=s.getLength(),a=i-1,l=0;i>l;l++){var p=s.getAt(l),g=s.getAt(a);(p.lng()<e.lng()&&g.lng()>=e.lng()||g.lng()<e.lng()&&p.lng()>=e.lng())&&p.lat()+(e.lng()-p.lng())/(g.lng()-p.lng())*(g.lat()-p.lat())<e.lat()&&(o=!o),a=l}return o}),google.maps.Circle.prototype.containsLatLng||(google.maps.Circle.prototype.containsLatLng=function(e){return google.maps.geometry?google.maps.geometry.spherical.computeDistanceBetween(this.getCenter(),e)<=this.getRadius():!0}),google.maps.LatLngBounds.prototype.containsLatLng=function(e){return this.contains(e)},google.maps.Marker.prototype.setFences=function(e){this.fences=e},google.maps.Marker.prototype.addFence=function(e){this.fences.push(e)},google.maps.Marker.prototype.getId=function(){return this.__gm_id},Array.prototype.indexOf||(Array.prototype.indexOf=function(e){"use strict";if(null==this)throw new TypeError;var t=Object(this),o=t.length>>>0;if(0===o)return-1;var n=0;if(arguments.length>1&&(n=Number(arguments[1]),n!=n?n=0:0!=n&&1/0!=n&&n!=-1/0&&(n=(n>0||-1)*Math.floor(Math.abs(n)))),n>=o)return-1;for(var r=n>=0?n:Math.max(o-Math.abs(n),0);o>r;r++)if(r in t&&t[r]===e)return r;return-1}),l});

var calendar, pseudoEventName = 'pseudoEvent',
    map, initLat = 48.7735272, initLng = 9.171102399999995,
    eventSubmitButton, eventDeleteButton,
    eventFormBootstrapValidator,
    invitedDivisionsSelectize,
    myVereinCLNDR =
    '<div class="controls">' +
        '<div class="clndr-previous-button">&lsaquo;</div><div class="month"><%= month %> <%= year %></div><div class="clndr-next-button">&rsaquo;</div>' +
    '</div>'+
    '<div class="days-container">'+
        '<div class="days">'+
            '<div class="headers">' +
                '<% _.each(daysOfTheWeek, function(day) { %>' +
                    '<div class="day-header"><%= day %></div>' +
                '<% }); %>' +
            '</div>' +
            '<% _.each(days, function(day, index) { %>' +
                '<div class="<%= day.classes %>" id="<%= day.id %>"><%= day.day %></div>' +
                '<% if(~day.classes.indexOf("calendar-dow-6")) { %>' +
                    '<br/>' +
                '<% } %>' +
            '<% }); %>' +
        '</div>' +
        '<div class="events">' +
            '<div class="headers">' +
                '<div class="event-header">EVENTS</div>' +
            '</div>' +
            '<div class="events-list">' +
                '<% _.each(eventsThisMonth, function(event) { ' +
                    'if(event.title != pseudoEventName) { %>' +
                        '<div class="event">' +
                            '<a href="/" data-id="<%= event.id %>" class="events eventClickable"><%= moment(event.startTime).format(\'Do MMM HH:mm\') %> - <%= moment(event.endTime).format(\'Do MMM HH:mm\') %>: <%= event.title %></a>' +
                        '</div>' +
                    '<% }' +
                '}); %>' +
            '</div>' +
        '</div>' +
    '</div>';

function resetEventForm(doNotHideDeleteButton) {
    $('#eventFlag').val('');
    $('#eventName').val('');
    $('#eventDescription').val('');
    $('#startDate').val('');
    $('#startTime').val('');
    $('#endDate').val('');
    $('#endTime').val('');
    $('#location').val('');
    $('#locationLat').val('');
    $('#locationLng').val('');

    //Reset divisions list
    invitedDivisionsSelectize[0].selectize.clear();

    //Reset map
    map.setCenter(initLat, initLng);
    map.removeMarkers();

    //Clear submit button
    $('#newEventButton').addClass('hidden');
    $('#oldEventButton').addClass('hidden');

    //Re-enable submit button
    eventSubmitButton.enable();
    eventDeleteButton.enable();

    if(!doNotHideDeleteButton) {
        //Hide delete button
        $('#eventDelete').addClass('hidden');
    }

    //Hide and clear heading
    $('#newEventHeading').addClass("hidden");
    $('#oldEventHeading').addClass("hidden");
    $('#oldEventHeadingName').empty();

    //Re-enable form
    eventFormBootstrapValidator.find('input').prop("disabled", false);
    $('#eventDescription').prop("disabled", false);
    invitedDivisionsSelectize[0].selectize.enable();

    //Reseting previous validation annotation
    eventFormBootstrapValidator.data('bootstrapValidator').resetForm();
}

//Set up everything to create a new event
function loadNewEvent(doNotHideDeleteButton) {
    resetEventForm(doNotHideDeleteButton);
    $('#eventFlag').val("true");
    $('#newEventHeading').removeClass('hidden');

    $('#newEventButton').removeClass('hidden');
    eventSubmitButton.enable();
    $('#eventName').focus();
}

//This function is called when either date is changed and sets the other date if it is not set yet.
function updateDates() {
    var startDate = $('#startDate'),
        endDate = $('#endDate');
    if(startDate.val() && !endDate.val())
    {
        $('#endDate').val(startDate.val());
    } else if(!startDate.val() && endDate.val())
    {
        startDate.val(endDate.val());
    }
}

//This function is called when either time is changed and sets the other time (one hour later/earlier) if it is not set yet. If the time set is at the date border, the time is copied.
function updateTimes() {
    var timesArray,
        startTime = $('#startTime'),
        endTime = $('#endTime');
    if(startTime.val() && !endTime.val())
    {
        timesArray = startTime.val().trim().split(":");
        if(timesArray[0].indexOf('24') == 0)
        {
            endTime.val(startTime.val());
        } else
        {
            endTime.val((parseInt(timesArray[0]) + 1) + ":" + timesArray[1]);
        }
    } else if(!startTime.val() && endTime.val())
    {
        timesArray = endTime.val().trim().split(":");
        if(timesArray[0].indexOf('0') == 0)
        {
            startTime.val(endTime.val());
        } else
        {
            startTime.val((parseInt(timesArray[0]) - 1) + ":" + timesArray[1]);
        }
    }
}

//Loads the occupied dates of a month, date needs to be a date object in UTC, if not it is converted, whose month and year are significant for the request.
function loadOccupiedDates(date)
{
    if(!date._isUTC) {
        date.add(date.utcOffset(), 'm');
    }
    $("#event-calendar-loading").addClass('heartbeat');
    $.getJSON("/event/getEventsOfMonth",
        {
            'month': date._d.getMonth() + 1, //Month is starting at 0
            'year': date._d.getFullYear()
        } ,
        function (data) {
            if(data && data.length) {
                var events = [];
                $.each(data, function (index, object) {
                    events.push({
                        date: localDateToString(object),
                        title: pseudoEventName
                    })
                });
                calendar.setEvents(events);
            }
            $("#event-calendar-loading").removeClass('heartbeat');
        }
    );
}

function loadDate(dateString)
{
    $("#event-calendar-loading").addClass('heartbeat');
    //Removing all non-pseudo elements
    calendar.removeEvents(function(event){
        return event.title != pseudoEventName;
    });
    $.getJSON("/event/getEventsOfDate",
        {
            date: dateString
        },
        function (data) {
            if(data && data.length) {
                var events = [];
                $.each(data, function (index, object) {
                    if(object.multiDate){
                        events.push({
                            start: localDateToString(object.startDateTime),
                            end: localDateToString(object.endDateTime),
                            startTime: localDateTimeToString(object.startDateTime),
                            endTime: localDateTimeToString(object.endDateTime),
                            title: object.name,
                            id: object.id
                        })
                    } else {
                        events.push({
                            date: dateString,
                            title: object.name,
                            startTime: localDateTimeToString(object.startDateTime),
                            endTime: localDateTimeToString(object.endDateTime),
                            id: object.id
                        });
                    }

                });
                calendar.addEvents(events);
            }
            $("#event-calendar-loading").removeClass('heartbeat');
        }
    );
}

function loadEvent(eventID) {
    eventSubmitButton.startAnimation();
    $.getJSON("/event/getEvent",
        {
            id: eventID
        },
        function(event) {
            if(event) {
                resetEventForm();
                var startDateTime = localDateTimeToString(event.startDateTime),
                    endDateTime = localDateTimeToString(event.endDateTime);
                $('#eventFlag').val(event.id);
                $('#eventName').val(event.name);
                $('#eventDescription').val(event.description);
                $('#startDate').val(moment(startDateTime).format('DD/MM/YYYY'));
                $('#startTime').val(moment(startDateTime).format('HH:mm'));
                $('#endDate').val(moment(endDateTime).format('DD/MM/YYYY'));
                $('#endTime').val(moment(endDateTime).format('HH:mm'));

                if(event.location)
                {
                    $('#location').val(event.location);
                    if(!event.locationLat && !event.locationLng)
                    {
                        updateMapUsingLocationField();
                    }
                }

                if(event.locationLat && event.locationLng)
                {
                    updateMapUsingLatLng(event.locationLat, event.locationLng);
                } else
                {
                    $('#locationLat').val('');
                    $('#locationLng').val('');
                }

                $('#newEventHeading').addClass("hidden");
                $('#oldEventHeading').removeClass("hidden");
                $('#oldEventHeadingName').text('<' + event.name + '>');

                $('#oldEventButton').removeClass("hidden");
                $('#eventDelete').removeClass('hidden');

                //Fill division list
                if (event.invitedDivision) {
                    $.each(event.invitedDivision, function (index, division) {
                        invitedDivisionsSelectize[0].selectize.addItem(division.name);
                    });
                }

                if(event.administrationNotAllowedMessage)
                {
                    disableEventForm();
                    showMessage(event.administrationNotAllowedMessage, 'warning', 'icon_error-triangle_alt');
                    eventSubmitButton.stopAnimation(1, function(button) {
                        button.disable();
                    });
                } else {
                    eventSubmitButton.stopAnimation(1, function(button) {
                        button.enable();
                    });
                }
            }
        });
}

//Disabling the user form, if a user is not allowed to manipulate the user
function disableEventForm(){
    $('#eventDelete').addClass('hidden');
    eventFormBootstrapValidator.find('input').prop("disabled", true);
    $('#eventDescription').prop("disabled", true);
    invitedDivisionsSelectize[0].selectize.disable();
}

function localDateToString(localDate) {
    if(localDate){
        return localDate.year + '-' + (localDate.monthValue < 10 ? "0" : "") + localDate.monthValue + '-' + (localDate.dayOfMonth < 10 ? "0" : "") + localDate.dayOfMonth;
    } else {
        return "";
    }
}

function localDateTimeToString(localDateTime) {
    if(localDateTime) {
        return localDateToString(localDateTime) + 'T' + (localDateTime.hour < 10 ? "0" : "") + localDateTime.hour + ':' + (localDateTime.minute < 10 ? "0" : "") + localDateTime.minute + ":00";
    } else {
        return "";
    }
}

function updateMapUsingLocationField() {
    GMaps.geocode({
        address: $('#location').val().trim(),
        callback: function (results, status) {
            if (status == 'OK') {
                var latlng = results[0].geometry.location;
                updateMapUsingLatLng(latlng.lat(), latlng.lng());
            }
        }
    });
}

function updateMapUsingLatLng(lat, lng)
{
    //GMap creates inline style, that sets the height to 0px
    $('#map').removeAttr('style');
    //Reset markers on the map
    map.removeMarkers();
    map.setCenter(lat, lng);
    map.addMarker({
        lat: lat,
        lng: lng
    });
    $('#locationLat').val(lat);
    $('#locationLng').val(lng);
    // k is lat, D is lng
    //console.log(map.markers[0].position);
}

function loadEventPage() {

    if(!calendar) {
        calendar = $('#calendar').clndr({
            template: myVereinCLNDR,
            clickEvents: {
                click: function (target) { //Every time the user clicks on a occupied date, the events of the date need to be loaded
                    if (target.events.length) {
                        loadDate(target.date._i);
                    }
                },
                onMonthChange: function(month) { //Every time the month is changed, the occupied dates need to be gathered
                    loadOccupiedDates(month);
                    resetEventForm();
                }
            },
            ready: function() { //Initially the current month needs to be loaded
                loadOccupiedDates(this.month)
            },
            doneRendering: function() {
                //Rebinding click events after the calendar was re-rendered
                $('.eventClickable').click(function(e){
                    e.preventDefault();
                    loadEvent($(this).data('id'));
                })
            },
            adjacentDaysChangeMonth: true,
            forceSixRows: true
        });

        // bind the calendar to the left and right arrow keys
        $(document).keydown(function (e) {
            if (e.keyCode == 37) {
                // left arrow
                calendar.back();
            }
            if (e.keyCode == 39) {
                // right arrow
                calendar.forward();
            }
        });
    }

    if(!($('#startTime').data().timepicker || $('#endTime').data().timepicker))
    {
        var eventTimePicker = $('.eventTimePicker'),
            timepickerOptions = {
                minuteStep: 5,
                defaultTime: false,
                showMeridian: false
            };

        eventTimePicker.timepicker(timepickerOptions);

        eventTimePicker.timepicker().on('hide.timepicker', function(e) {
            updateTimes();
        });

        eventTimePicker.focusout(function(e){
            updateTimes();
        })
    }


    if(!($('#startDate').data().datepicker || $('#endDate').data().datepicker)) {
        //Enable Datepicker
        var eventDatePicker = $('.eventDatePicker'),
            datepickerOptions = {
            format: "dd/mm/yyyy",
            language: locale,
            todayHighlight: true
        };

        eventDatePicker.datepicker(datepickerOptions);

        eventDatePicker.focusout(function(){
            updateDates();
        });
    }

    if(!map)
    {
        map = new GMaps({
            div: '#map',
            lat: initLat,
            lng: initLng
        });
        $('#location').keyup(function(e){
            if($('#location').val().trim().length > 5) {
                updateMapUsingLocationField();
            }
        });
    }

    if(!(invitedDivisionsSelectize = $('#invitedDivisions'))[0].selectize) {
        //Configuring division input field
        invitedDivisionsSelectize.selectize({
            persist: false,
            createOnBlur: true,
            create: false, //Not allowing the creation of user specific items
            hideSelected: true, //If an option is allready in the list it is hidden
            preload: true, //Loading data immidiately (if user is loaded without loading the available divisions, the added divisions get removed because selectize thinks they are not valid)
            valueField: 'name',
            labelField: 'name',
            searchField: 'name',
            load: function (query, callback) {
                $.ajax({
                    url: '/division/getDivision',
                    type: 'GET',
                    data: {
                        term: query
                    },
                    error: function () {
                        callback();
                    },
                    success: function (data) {
                        callback(data);
                    }
                });
            }
        });
    } else
    {
        //Update entries within selectize list
        invitedDivisionsSelectize[0].selectize.load(function (callback) {
            $.ajax({
                url: '/division/getDivision',
                type: 'GET',
                error: function () {
                    callback();
                },
                success: function (data) {
                    callback(data);
                }
            });
        });
    }

    if(!(eventFormBootstrapValidator = $('#eventForm')).data('bootstrapValidator')) {
        //Enable bootstrap validator
        eventFormBootstrapValidator.bootstrapValidator({
            excluded: [':disabled', ':hidden', ':not(:visible)']
                }) //The constrains are configured within the HTML
                    .on('success.form.bv', function (e) { //The submission function
                        // Prevent form submission
                        e.preventDefault();
                        //Starting button animation
                        eventSubmitButton.startAnimation();
                        //Send the serialized form
                        $.ajax({
                            url: '/event',
                            type: 'POST',
                            data: $(e.target).serialize(),
                            error: function (response) {
                                eventSubmitButton.stopAnimation(-1);
                                showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                            },
                            success: function (response) {
                                eventSubmitButton.stopAnimation(0);
                                showMessage(response, 'success', 'icon_check');
                                loadNewEvent();
                                loadOccupiedDates(calendar.month);
                            }
                        });
                    });
    }

    if(!eventSubmitButton) {
        //Enabling progress button
        eventSubmitButton = new UIProgressButton(document.getElementById('eventSubmitButton'));
    }

    if(!eventDeleteButton) {
        //Enabling progress button
        eventDeleteButton = new UIProgressButton(document.getElementById('eventDelete'));
        $('#eventDeleteButton').click(function(e){
            e.preventDefault();
            eventDeleteButton.startAnimation();
            $.ajax({
                url: '/event/deleteEvent',
                type: 'POST',
                data: {
                    id: $('#eventFlag').val()
                },
                error: function (response) {
                    eventDeleteButton.stopAnimation(-1);
                    showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                },
                success: function (response) {
                    eventDeleteButton.stopAnimation(0, function(button) {
                        classie.add(button.el, 'hidden');
                    });
                    showMessage(response, 'success', 'icon_check');
                    loadNewEvent(true);
                    loadOccupiedDates(calendar.month);
                }
            });
        })
    }

    $('#addEvent').click(function(){
        resetEventForm();
        loadNewEvent();
    });

    resetEventForm();
    loadNewEvent();
}