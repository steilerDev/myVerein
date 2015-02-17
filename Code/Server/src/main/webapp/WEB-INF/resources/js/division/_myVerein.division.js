/**
 * Document   : myVerein.division.js
 * Description: This JavaScript file contains all methods needed by the division page.
 * Copyright  : (c) 2014 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

var divisionSubmitButton,
    divisionDeleteButton,
    adminSelectize,
    divisionFormBootstrapValidator,
    divisionTree,
    clubName;

function resetDivisionForm(doNotHideDeleteButton) {
    $('#name').val('');
    $('#description').val('');
    adminSelectize[0].selectize.clear();
    $('#oldName').val('');

    //Reset heading
    $('#initDivisionHeading').removeClass('hidden');
    $('#newDivisionHeading').addClass('hidden');
    $('#oldDivisionHeading').addClass('hidden');
    $('#oldDivisionHeadingName').empty();

    //Re-enable form
    divisionFormBootstrapValidator.find('input').prop("disabled", false);
    adminSelectize[0].selectize.enable();
    divisionSubmitButton.enable();
    divisionDeleteButton.enable();

    if(!doNotHideDeleteButton) {
        //Hide delete button
        $('#divisionDelete').addClass('hidden');
    }

    //Reseting previous validation annotation
    divisionFormBootstrapValidator.data('bootstrapValidator').resetForm();
}

function disableDivisionForm() {
    divisionFormBootstrapValidator.find('input').prop("disabled", true);
    adminSelectize[0].selectize.disable();
    divisionSubmitButton.disable();
    divisionDeleteButton.disable();
}

//Loading division information into the form
function loadDivision(name, newDivision) {
    resetDivisionForm();
    divisionSubmitButton.startAnimation();
    //Sending JSON request with the division name as parameter to get the division details
    $.ajax({
        url: '/division',
        type: 'GET',
        data: {
            name: name
        },
        error: function () {
            divisionSubmitButton.stopAnimation(-1, function(button){
                button.disable();
            });
            disableDivisionForm();
        },
        success: function (response) {
            resetDivisionForm();

            $('#oldName').val(response.name);

            var name = $('#name');
            name.focus();

            if(newDivision)
            {
                $('#newDivisionHeading').removeClass('hidden');
            } else
            {
                name.val(response.name);
                $('#description').val(response.desc);
                $('#oldDivisionHeading').removeClass('hidden');

                if (response.adminUser) {
                    adminSelectize[0].selectize.addItem(response.adminUser.email);
                }
            }

            $('#oldDivisionHeadingName').text('<' + response.name + '>');
            $('#initDivisionHeading').addClass('hidden');
            $('#divisionDelete').removeClass('hidden');

            divisionSubmitButton.stopAnimation(1);
        }
    });
}

function loadTree(callback) {
    $('#division-tree-loading').addClass('heartbeat');
    //Loading tree through ajax
    divisionTree.tree(
        'loadDataFromUrl',
        '/division/divisionTree', //URL
        null, //Replace existing tree
        function() {
            $('#division-tree-loading').removeClass('heartbeat'); //Stop loading animation when successful
            if(callback){
                callback();
            }
        }
    );

}

//This function is called as soon as the tab is shown. If necessary it is loading all required resources.
function loadDivisionPage() {

    //Getting the club name, to compare it to the tree nodes. The node with the club name is not allowed to be selected.
    $.ajax({
        url: '/content/clubName',
        type: 'GET',
        error: function () {
            showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
        },
        success: function (data) {
            clubName = data;
        }
    });

    if (!(divisionTree = $('#division-tree')).data().simple_widget_tree) {
        //Configure division tree
        divisionTree.tree({
            autoOpen: true,
            dragAndDrop: true,
            onCanMove: function (node) {
                //Not allowed to move a root node
                return node.parent.parent;
            },
            onCanMoveTo: function (moved_node, target_node, position) {
                //User is not allowed to move a node to a new root position
                return !(!target_node.parent.parent && (position == 'before' || position == 'after'));
            },
            onLoadFailed: function (response) {
                showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                $('#division-tree-loading').removeClass('heartbeat');
            },
            onCanSelectNode: function(node) {
                return node.name != clubName;
            }
        });

        //Clicking on a tree node needs to fill the form
        divisionTree.bind(
            'tree.click',
            function (event) {
                if(event.node.name != clubName) {
                    loadDivision(event.node.name);
                }
            }
        );

        divisionTree.bind(
            'tree.move',
            function (event) {
                $.ajax({
                    url: '/division/divisionTree',
                    type: 'POST',
                    data: {
                        moved_node: event.move_info.moved_node.name,
                        target_node: event.move_info.target_node.name,
                        position: event.move_info.position,
                        previous_parent: event.move_info.previous_parent.name
                    },
                    error: function (response) {
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                        loadTree()
                    },
                    success: function (response) {
                        //console.log(response);
                    }
                });
            }
        );
    }

    if (!(adminSelectize = $('#admin'))[0].selectize) {
        //Enabling selection of admin userfrom existing user
        adminSelectize.selectize({
            persist: false,
            createOnBlur: true,
            create: false, //Not allowing the creation of user specific items
            hideSelected: true, //If an option is allready in the list it is hidden
            preload: true, //Loading data immidiately (if division is loaded without loading the available user, the added user gets removed because selectize thinks he is not valid)
            valueField: 'email',
            labelField: 'email',
            searchField: 'email',
            disable: true,
            maxItems: 1,
            render: {
                option: function (item, escape) {
                    return '<div>' +
                        '<span class="name">' + escape(item.firstName) + ' ' + escape(item.lastName) + ' </span>' +
                        '<span class="description">(' + escape(item.email) + ')</span>' +
                        '</div>';
                }
            },
            load: function (query, callback) {
                $.ajax({
                    url: '/user',
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
        adminSelectize[0].selectize.load(function (callback) {
            $.ajax({
                url: '/user',
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

    if (!(divisionFormBootstrapValidator = $('#divisionForm')).data('bootstrapValidator')) {
        //Enable bootstrap validator
        divisionFormBootstrapValidator.bootstrapValidator() //The constrains are configured within the HTML
            .on('success.form.bv', function (e) { //The submit function
                // Prevent form submission
                e.preventDefault();
                //Starting button animation
                divisionSubmitButton.startAnimation();
                //Send the serialized form
                $.ajax({
                    url: '/division',
                    type: 'POST',
                    data: $(e.target).serialize(),
                    error: function (response) {
                        divisionSubmitButton.stopAnimation(-1);
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                    },
                    success: function (response) {
                        divisionSubmitButton.stopAnimation(1);
                        showMessage(response, 'success', 'icon_check');
                        $('#oldName').val($('#name').val()); //If the name changed and the division is not reloaded the oldName needs to be resetted
                        loadTree();
                    }
                });
            });
    }

    if (!divisionSubmitButton) {
        //Enabling progress button
        divisionSubmitButton = new UIProgressButton(document.getElementById('divisionSubmitButton'));
    }

    if(!divisionDeleteButton) {
        //Enabling progress button
        divisionDeleteButton = new UIProgressButton(document.getElementById('divisionDelete'));
        $('#divisionDeleteButton').click(function(e){
            e.preventDefault();
            divisionDeleteButton.startAnimation();
            $.ajax({
                url: '/division?divisionName=' + $('#oldName').val(), //Workaround since DELETE request needs to be identified by the URI only and jQuery is not attaching the data to the URI, which leads to a Spring error.
                type: 'DELETE',
                //data: {
                //    divisionName: $('#oldName').val()
                //},
                error: function (response) {
                    divisionDeleteButton.stopAnimation(-1);
                    showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                },
                success: function (response) {
                    divisionDeleteButton.stopAnimation(1, function(button){
                        classie.add(button.el, 'hidden');
                    });
                    showMessage(response, 'success', 'icon_check');
                    resetDivisionForm(true);
                    disableDivisionForm();
                    loadTree();
                }
            });
        })
    }

    $('#addDivision').click(function(e){
        divisionSubmitButton.startAnimation();
        $.ajax({
            url: '/division',
            type: 'POST',
            data: {
                'new': true
            },
            error: function(response) {
                loadTree();
                resetDivisionForm();
                divisionSubmitButton.stopAnimation(-1);
                showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
            },
            success: function(response) {
                divisionSubmitButton.stopAnimation(1);
                //Separating response message and name of the new division
                var splitResponse = response.split("||");
                loadDivision(splitResponse[1], true);
                loadTree(function(){
                    divisionTree.tree('selectNode', divisionTree.tree('getNodeByName', splitResponse[1]));
                });
                showMessage(splitResponse[0], 'success', 'icon_check');
            }
        });
    });

    loadTree();
    resetDivisionForm();
    disableDivisionForm();
}