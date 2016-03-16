/**
 * @author Robin Duda
 */


var application = {
    authentication: null,
    handlers: {},

    authenticated: function (authentication) {
        console.log(authentication);

        this.authentication = authentication;
        $('#login-panel').hide();
        $('#admin-panel').show();

        this.publish('onAuthentication', authentication);
    },

    logout: function () {
        authentication = null;
        $('#login-panel').show();
        $('#admin-panel').hide();
    },

    onAuthentication: function (callback) {
        this.subscribe('onAuthentication', callback);
    },

    subscribe: function (event, callback) {
        if (this.handlers[event] == null)
            this.handlers[event] = [];

        this.handlers[event].push(callback);
    },

    publish: function (event, data) {
        for (var subscriber = 0; subscriber < this.handlers[event].length; subscriber++)
            this.handlers[event][subscriber](data);
    }
};


$(document).ready(function () {
    $('#admin-panel').hide();
});