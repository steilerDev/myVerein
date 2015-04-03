/**
 * uiProgressButton.js v1.0.0 adjustments by Frank Steiler (c) 2015
 * http://www.codrops.com
 *
 * Licensed under the MIT license.
 * http://www.opensource.org/licenses/mit-license.php
 *
 * Copyright 2014, Codrops
 * http://www.codrops.com
 */

;( function( window ) {

    'use strict';

    var transEndEventNames = {
            'WebkitTransition': 'webkitTransitionEnd',
            'MozTransition': 'transitionend',
            'OTransition': 'oTransitionEnd',
            'msTransition': 'MSTransitionEnd',
            'transition': 'transitionend'
        },
        transEndEventName = transEndEventNames[ Modernizr.prefixed( 'transition' ) ],
        support = { transitions : Modernizr.csstransitions };

    function extend( a, b ) {
        for( var key in b ) {
            if( b.hasOwnProperty( key ) ) {
                a[key] = b[key];
            }
        }
        return a;
    }

    function SVGEl( el ) {
        this.el = el;
        // the path elements
        this.paths = [].slice.call( this.el.querySelectorAll( 'path' ) );
        // we will save both paths and its lengths in arrays
        this.pathsArr = [];
        this.lengthsArr = [];
        this._init();
    }

    SVGEl.prototype._init = function() {
        var self = this;
        this.paths.forEach( function( path, i ) {
            self.pathsArr[i] = path;
            path.style.strokeDasharray = self.lengthsArr[i] = path.getTotalLength();
        } );
        // undraw stroke
        this.draw(0);
    };

    // val in [0,1] : 0 - no stroke is visible, 1 - stroke is visible
    SVGEl.prototype.draw = function( val ) {
        for( var i = 0, len = this.pathsArr.length; i < len; ++i ){
            this.pathsArr[ i ].style.strokeDashoffset = this.lengthsArr[ i ] * ( 1 - val );
        }
    };

    function UIProgressButton( el, options ) {
        this.el = el;
        this.options = extend( {}, this.options );
        extend( this.options, options );
        this._init();
    }

    UIProgressButton.prototype.options = {
        // time in ms that the status (success or error will be displayed) - should be at least higher than the transition-duration value defined for the stroke-dashoffset transition of both checkmark and cross strokes
        statusTime : 1500
    };

    UIProgressButton.prototype._init = function() {
        // the button
        this.button = this.el.querySelector( 'button' );
        // progress el
        this.progressEl = new SVGEl( this.el.querySelector( 'svg.progress-circle' ) );
        // the success/error elems
        this.successEl = new SVGEl( this.el.querySelector( 'svg.checkmark' ) );
        this.errorEl = new SVGEl( this.el.querySelector( 'svg.cross' ) );
        // enable button
        this.enable();
    };

    //The function is starting the animation up to 70percent of the circle. The rest is then filled when the callback from the request comes in.
    UIProgressButton.prototype.startAnimation = function() {
        // by adding the loading class the button will transition to a "circle"
        classie.addClass( this.el, 'loading' );
        var self = this,
            onEndBtnTransitionFn = function( ev ) {
                if( support.transitions ) {
                    if( ev.propertyName !== 'width' ) return false;
                    this.removeEventListener( transEndEventName, onEndBtnTransitionFn );
                }
                // disable the button - this should have been the first thing to do when clicking the button.
                // however if we do so Firefox does not seem to fire the transitionend event.
                self.disable();

                var progress = 0,
                    interval = setInterval(function () {
                        progress = Math.min(progress + Math.random() * 0.1, 1);
                        self.setProgress(progress);

                        if (progress >= 0.8) {
                            clearInterval(interval);
                        }
                    }, 150);
            };

        if( support.transitions ) {
            this.button.addEventListener( transEndEventName, onEndBtnTransitionFn );
        }
        else {
            onEndBtnTransitionFn();
        }
    };

    // runs after the progress reaches 100%, if status >= 0 success, failure otherwise
    UIProgressButton.prototype.stopAnimation = function( status, callback ) {
        var self = this,
            endLoading = function() {
                self.setProgress(1);
                // first undraw progress stroke.
                self.progressEl.draw(0);

                if( typeof status === 'number' ) {
                    var statusClass = status >= 0 ? 'success' : 'error',
                        statusEl = status >=0 ? self.successEl : self.errorEl;

                    // draw stroke of success (checkmark) or error (cross).
                    statusEl.draw( 1 );
                    // add respective class to the element
                    classie.addClass( self.el, statusClass );
                    // after options.statusTime remove status and undraw the respective stroke. Also enable the button.
                    setTimeout( function() {
                        classie.remove( self.el, statusClass );
                        statusEl.draw(0);
                        //Automatically hides error buttons
                        if(callback) {
                            callback(self);
                        } else
                        {
                            self.enable();
                        }
                    }, self.options.statusTime );
                }
                // finally remove class loading and reset progress.
                self.setProgress(0);
                classie.removeClass( self.el, 'loading' );
            };

        // give it a time (ideally the same like the transition time) so that the last progress increment animation is still visible.
        setTimeout( endLoading, 300 );
    };

    UIProgressButton.prototype.setProgress = function( val ) {
        this.progressEl.draw( val );
    };

    // enable button
    UIProgressButton.prototype.enable = function() {
        classie.removeClass(this.el, 'progress-button-disabled');
        this.button.removeAttribute( 'disabled' );
    };

    // disable button
    UIProgressButton.prototype.disable = function() {
        classie.add(this.el, 'progress-button-disabled');
        this.button.setAttribute( 'disabled', '' );
    };

    // add to global namespace
    window.UIProgressButton = UIProgressButton;

})( window );