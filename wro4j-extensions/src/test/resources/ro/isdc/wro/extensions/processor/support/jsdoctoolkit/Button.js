/**
 * Constructs a button.
 *
 * @name Button
 * @version 0.1
 * @param {jQuery} $element jQuery element to render
 * @constructor
 * @requires $ (JQuery)
 */
function Button($element) {
	/**
	 * The button's JQuery element
	 * @type JQuery
	 * @memberOf Button#
	 * @name $el
	 */
	this.$el = $element;
	/**
	 * The button's ID
	 * @type String
	 * @memberOf Button#
	 * @name id
	 */
	this.id = 'a-button-id';
	/**
	 * The button's text / localised label
	 * @type String
	 * @memberOf Button#
	 * @name text
	 */
	this.text = 'Click Me!';
	/**
	 * Whether the button is enabled
	 * @private
	 * @type boolean
	 * @memberOf Button#
	 * @name enabled
	 */
	this.enabled = true;


	this.$el.click(function() {
		alert('You clicked me');
	});

	this.render();
}

/**
 * Renders the button.
 *
 * @function
 * @return {void}
 */
Button.prototype.render = function() {
	this.$el.html("<button></button>")
};

/**
 * Sets if the button is enabled.
 *
 * @function
 * @param {boolean} enabled
 * @return {void}
 */
Button.prototype.setEnabled = function(enabled) {
	if (this.enabled != enabled)
	{
		this.enabled = enabled === true || enabled == 'true';
		this.render();
	}
};

/**
 * Returns true if the button is enabled.
 *
 * @function
 * @return {boolean} is enabled
 */
Button.prototype.isEnabled = function() {
	return this.enabled;
};