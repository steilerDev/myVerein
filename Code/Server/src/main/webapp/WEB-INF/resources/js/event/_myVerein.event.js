/**
 * Document   : myVerein.event.js
 * Description: This JavaScript file contains all methods needed by the event page.
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

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
    if(map) {
        map.setCenter(initLat, initLng);
        map.removeMarkers();
    }

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
    $.ajax({
        url: '/api/admin/event/month',
        type: 'GET',
        data: {
            'month': date._d.getMonth() + 1, //Month is starting at 0
            'year': date._d.getFullYear()
        },
        error: function (response) {
            //console.log(response.responseText);
            $("#event-calendar-loading").removeClass('heartbeat');
        },
        success: function (data) {
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
    });
}

function loadDate(dateString)
{
    $("#event-calendar-loading").addClass('heartbeat');
    //Removing all non-pseudo elements
    calendar.removeEvents(function(event){
        return event.title != pseudoEventName;
    });

    $.ajax({
        url: '/api/admin/event/date',
        type: 'GET',
        data: {
            date: dateString
        },
        error: function (response) {
            //console.log(response.responseText);
            $("#event-calendar-loading").removeClass('heartbeat');
        },
        success: function (data) {
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
    });
}

function loadEvent(eventID) {
    eventSubmitButton.startAnimation();
    $.ajax({
        url: '/api/admin/event',
        type: 'GET',
        data: {
            id: eventID
        },
        error: function (response) {
            //console.log(response.responseText);
            eventSubmitButton.stopAnimation(-1);
        },
        success: function (event) {
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
                eventSubmitButton.stopAnimation(1);
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
    if(map) {
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
}

function updateMapUsingLatLng(lat, lng)
{
    if(map) {
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
        if(typeof window.google === 'object' && window.google.maps) {
            $('#map').removeClass('hidden');
            map = new GMaps({
                div: '#map',
                lat: initLat,
                lng: initLng
            });
            $('#location').keyup(function (e) {
                if ($('#location').val().trim().length > 5) {
                    updateMapUsingLocationField();
                }
            });
        } else
        {
            $('#map').addClass('hidden');
            showMessage(getLocalizedString("mapsNotAvailable"), 'error', 'icon_error-triangle_alt');
        }
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
                    url: '/api/admin/division',
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
        invitedDivisionsSelectize[0].selectize.clearOptions();
        invitedDivisionsSelectize[0].selectize.load(function (callback) {
            $.ajax({
                url: '/api/admin/division',
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
                            url: '/api/admin/event',
                            type: 'POST',
                            data: $(e.target).serialize(),
                            error: function (response) {
                                eventSubmitButton.stopAnimation(-1);
                                showMessage(response.responseJSON.errorMessage, 'error', 'icon_error-triangle_alt');
                            },
                            success: function (response) {
                                eventSubmitButton.stopAnimation(1);
                                loadEvent(response.eventID);
                                loadOccupiedDates(calendar.month);
                                showMessage(response.successMessage, 'success', 'icon_check');
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
                url: '/api/admin/event?id=' + $('#eventFlag').val(), //Workaround since DELETE request needs to be identified by the URI only and jQuery is not attaching the data to the URI, which leads to a Spring error.
                type: 'DELETE',
                //data: {
                //    id: $('#eventFlag').val()
                //},
                error: function (response) {
                    eventDeleteButton.stopAnimation(-1);
                    showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                },
                success: function (response) {
                    eventDeleteButton.stopAnimation(1, function(button) {
                        classie.add(button.el, 'hidden');
                        button.enable();
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