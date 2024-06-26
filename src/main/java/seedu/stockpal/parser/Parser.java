package seedu.stockpal.parser;

import seedu.stockpal.commands.HelpCommand;
import seedu.stockpal.commands.ListCommand;
import seedu.stockpal.commands.ExitCommand;
import seedu.stockpal.commands.NewCommand;
import seedu.stockpal.commands.EditCommand;
import seedu.stockpal.commands.DeleteCommand;
import seedu.stockpal.commands.InflowCommand;
import seedu.stockpal.commands.OutflowCommand;
import seedu.stockpal.commands.FindCommand;
import seedu.stockpal.commands.HistoryCommand;
import seedu.stockpal.commands.Command;

import seedu.stockpal.exceptions.InvalidCommandException;
import seedu.stockpal.exceptions.InvalidFormatException;
import seedu.stockpal.exceptions.UnsignedIntegerExceededException;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

import static seedu.stockpal.common.Messages.EMPTY_STRING;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_CENTS_EXCEED_LENGTH;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_DOLLAR_EXCEED_MAX_INT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_EMPTY_AMOUNT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_EMPTY_DESCRIPTION;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_EMPTY_NAME;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_EMPTY_PRICE;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_EMPTY_QUANTITY;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INPUT_INTEGER_EXCEEDED;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INT_PARSE_FAIL;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_COMMAND;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_DELETE_FORMAT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_DESCRIPTION_LENGTH;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_EDIT_FORMAT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_EXIT_USAGE;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_FIND_FORMAT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_HISTORY_FORMAT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_INFLOW_FORMAT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_NAME_LENGTH;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_NEW_FORMAT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_OUTFLOW_FORMAT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_PID_FORMAT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_PRICE_FORMAT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_KEYWORD_ILLEGAL_CHAR;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_MISSING_AMOUNT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_MISSING_AMOUNT_FLAG;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_MISSING_NAME_FLAG;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_MISSING_PRICE;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_MISSING_QUANTITY;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_MISSING_QUANTITY_FLAG;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_NAME_ILLEGAL_CHAR;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_DESCRIPTION_ILLEGAL_CHAR;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_ZERO_AMOUNT;
import static seedu.stockpal.common.Messages.MESSAGE_ERROR_INVALID_LIST_SPECIFIER;

/**
 * The Parser class is responsible for parsing user input into its respective command's relevant fields.
 */
public class Parser {
    public static final String DIVIDER = " ";
    private static final String NAME_FLAG = "n/";
    private static final String QUANTITY_FLAG = "q/";
    private static final String PRICE_FLAG = "p/";
    private static final String DESCRIPTION_FLAG = "d/";
    private static final String AMOUNT_FLAG = "a/";
    private static final String ARG_GROUP = "ARG";
    private static final String INTEGER_GROUP = "INT";
    private static final String FRACTIONAL_GROUP = "FRAC";
    private static final Pattern PID_PATTERN = Pattern.compile("^(?<" + ARG_GROUP + ">\\d+)( .*)?");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^(n/|N/)(?<" + ARG_GROUP + ">[a-zA-Z0-9 ()\\[\\],.\\-_]+)?" +
            "( (q/|Q/).*| (p/|P/).*| (d/|D/).*)?");
    private static final Pattern QUANTITY_PATTERN =
            Pattern.compile("^(q/|Q/)(?<"+ ARG_GROUP +">.+?)( (p/|P/).*| (d/|D/).*)?");
    private static final Pattern PRICE_PATTERN = Pattern.compile("^(p/|P/)(?<"+ ARG_GROUP + ">.+?)( (d/|D/).*)?");
    private static final Pattern DESCRIPTION_PATTERN =
            Pattern.compile("^(d/|D/)(?<" + ARG_GROUP + ">[a-zA-Z0-9 ()\\[\\],.\\-_]+)?");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^(a/|A/)(?<" + ARG_GROUP + ">.+)");
    private static final Pattern LIST_FLAG_PATTERN = Pattern.compile("(?<" + ARG_GROUP + ">-sn|-sq)?");
    private static final Pattern KEYWORD_PATTERN =
            Pattern.compile("(?<" + ARG_GROUP + ">[a-zA-Z0-9 ()\\[\\],.\\-_]+)?");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern DOUBLE_PATTERN =
            Pattern.compile("(?<" + INTEGER_GROUP + ">\\d+)(?<" + FRACTIONAL_GROUP + ">\\.\\d+)?");
    private static final int FLAG_LENGTH = 2;
    private static final int START_INDEX = 0;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 100;
    private static final int ZERO_AMOUNT = 0;
    private static final int DECIMAL_LENGTH = 3;
    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());
    private static String getCommandFromInput(String input) {
        if (!input.contains(DIVIDER)) {
            return input.toLowerCase();
        }

        return input.substring(START_INDEX, input.indexOf(DIVIDER)).toLowerCase();
    }

    private Integer parseInteger(String intString) throws InvalidFormatException, UnsignedIntegerExceededException {
        Matcher intMatcher = INTEGER_PATTERN.matcher(intString);
        if (!intMatcher.matches()) {
            throw new InvalidFormatException(MESSAGE_ERROR_INT_PARSE_FAIL);
        }

        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException nfe) {
            throw new UnsignedIntegerExceededException(MESSAGE_ERROR_INPUT_INTEGER_EXCEEDED);
        }
    }
    private Double parsePrice(String matchedPrice) throws InvalidFormatException {
        Matcher doubleMatcher = DOUBLE_PATTERN.matcher(matchedPrice);
        if (!doubleMatcher.matches()) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_PRICE_FORMAT);
        }

        Integer dollar;

        String dollarString = doubleMatcher.group(INTEGER_GROUP);
        String centsString = doubleMatcher.group(FRACTIONAL_GROUP);

        try {
            dollar = Integer.parseInt(dollarString);
        } catch (NumberFormatException nfe) {
            throw new InvalidFormatException(MESSAGE_ERROR_DOLLAR_EXCEED_MAX_INT);
        }

        if (isNull(centsString)) {
            return Double.valueOf(dollar);
        }

        if (centsString.length() > DECIMAL_LENGTH) {
            throw new InvalidFormatException(MESSAGE_ERROR_CENTS_EXCEED_LENGTH);
        }

        return Double.parseDouble(matchedPrice);
    }

    private String matchPrice(String input) throws InvalidFormatException {
        Matcher priceMatcher = PRICE_PATTERN.matcher(input);
        if (!priceMatcher.matches()) {
            throw new InvalidFormatException(MESSAGE_ERROR_MISSING_PRICE);
        }

        String matchedString = priceMatcher.group(ARG_GROUP);

        if (matchedString.isBlank()) {
            throw new InvalidFormatException(MESSAGE_ERROR_EMPTY_PRICE);
        }
        return matchedString;
    }
    private String matchPid(String input) throws InvalidFormatException {
        Matcher pidMatcher = PID_PATTERN.matcher(input);
        if (!pidMatcher.matches()) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_PID_FORMAT);
        }
        
        return pidMatcher.group(ARG_GROUP);
    }

    private String matchAmount(String input) throws InvalidFormatException {
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(input);
        if (!amountMatcher.matches()) {
            throw new InvalidFormatException(MESSAGE_ERROR_MISSING_AMOUNT);
        }

        String matchedAmount = amountMatcher.group(ARG_GROUP);

        if (matchedAmount.isBlank()) {
            throw new InvalidFormatException(MESSAGE_ERROR_EMPTY_AMOUNT);
        }
        
        return matchedAmount;
    }

    private String matchQuantity(String input) throws InvalidFormatException {
        Matcher quantityMatcher = QUANTITY_PATTERN.matcher(input);
        if (!quantityMatcher.matches()) {
            throw new InvalidFormatException(MESSAGE_ERROR_MISSING_QUANTITY);
        }

        String matchedQuantity = quantityMatcher.group(ARG_GROUP);

        if (matchedQuantity.isBlank()) {
            throw new InvalidFormatException(MESSAGE_ERROR_EMPTY_QUANTITY);
        }

        return matchedQuantity;
    }

    private String matchString(Pattern pattern, String input) {
        Matcher stringMatcher = pattern.matcher(input);
        if (!stringMatcher.matches()) {
            return null;
        }
        
        if (isNull(stringMatcher.group(ARG_GROUP))) { //matched but is nothing is captured
            return EMPTY_STRING;
        }
        
        return stringMatcher.group(ARG_GROUP);
    }

    private static void validateName(String matchedName) throws InvalidFormatException {
        if (isNull(matchedName)) {
            throw new InvalidFormatException(MESSAGE_ERROR_NAME_ILLEGAL_CHAR);
        }
        if (matchedName.isBlank()) {
            throw new InvalidFormatException(MESSAGE_ERROR_EMPTY_NAME);
        }
        if (matchedName.strip().length() > MAX_NAME_LENGTH) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_NAME_LENGTH);
        }
    }

    private static void validateDescription(String matchedDescription) throws InvalidFormatException {
        if (isNull(matchedDescription)) {
            throw new InvalidFormatException(MESSAGE_ERROR_DESCRIPTION_ILLEGAL_CHAR);
        }

        if (matchedDescription.strip().length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_DESCRIPTION_LENGTH);
        }

        if (matchedDescription.isBlank()) {
            throw new InvalidFormatException(MESSAGE_ERROR_EMPTY_DESCRIPTION);
        }
    }

    private static void validateFindKeyword(String keyword) throws InvalidFormatException {
        if (isNull(keyword)) {
            throw new InvalidFormatException(MESSAGE_ERROR_KEYWORD_ILLEGAL_CHAR);
        }

        if (keyword.isBlank()) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_FIND_FORMAT);
        }
    }

    private ExitCommand createExitCommand(String input) throws InvalidFormatException {
        input = input.strip();
        if (input.equals(ExitCommand.COMMAND_KEYWORD)) {
            return new ExitCommand();
        }
        throw new InvalidFormatException(MESSAGE_ERROR_INVALID_EXIT_USAGE);
    }

    private ListCommand createListCommand(String input) throws InvalidFormatException {
        input = input.substring(ListCommand.COMMAND_KEYWORD.length()).strip();
        Matcher listSpecifierMatcher = LIST_FLAG_PATTERN.matcher(input.toLowerCase());

        if (input.isEmpty()) {
            return new ListCommand(null);
        }

        if (listSpecifierMatcher.matches()) {
            return new ListCommand(listSpecifierMatcher.group(ARG_GROUP));
        }

        throw new InvalidFormatException(MESSAGE_ERROR_INVALID_LIST_SPECIFIER);
    }

    private OutflowCommand createOutflowCommand(String input)
            throws InvalidFormatException, UnsignedIntegerExceededException {
        input = input.substring(OutflowCommand.COMMAND_KEYWORD.length()).stripLeading();

        String pidString = matchPid(input);
        Integer pid = parseInteger(pidString);
        input = input.substring(pidString.length()).stripLeading();

        if (!input.toLowerCase().startsWith(AMOUNT_FLAG)) {
            throw new InvalidFormatException(MESSAGE_ERROR_MISSING_AMOUNT_FLAG);
        }
        String amountString = matchAmount(input);
        assert amountString != null;
        Integer decreaseBy = parseInteger(amountString.stripLeading());
        if (decreaseBy == ZERO_AMOUNT) {
            throw new InvalidFormatException(MESSAGE_ERROR_ZERO_AMOUNT);
        }
        input = input.substring(amountString.length() + FLAG_LENGTH).stripLeading();

        if (!input.isEmpty()) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_OUTFLOW_FORMAT);
        }

        return new OutflowCommand(pid, decreaseBy);
    }

    private InflowCommand createInflowCommand(String input)
            throws InvalidFormatException, UnsignedIntegerExceededException {
        input = input.substring(InflowCommand.COMMAND_KEYWORD.length()).stripLeading();

        String pidString = matchPid(input);
        Integer pid = parseInteger(pidString);
        input = input.substring(pidString.length()).stripLeading();

        if (!input.toLowerCase().startsWith(AMOUNT_FLAG)) {
            throw new InvalidFormatException(MESSAGE_ERROR_MISSING_AMOUNT_FLAG);
        }
        String amountString = matchAmount(input);
        assert amountString != null;
        Integer increaseBy = parseInteger(amountString.stripLeading());
        if (increaseBy == ZERO_AMOUNT) {
            throw new InvalidFormatException(MESSAGE_ERROR_ZERO_AMOUNT);
        }
        input = input.substring(amountString.length() + FLAG_LENGTH).stripLeading();

        if (!input.isEmpty()) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_INFLOW_FORMAT);
        }

        return new InflowCommand(pid, increaseBy);
    }


    private DeleteCommand createDeleteCommand(String input)
            throws InvalidFormatException, UnsignedIntegerExceededException {
        input = input.substring(DeleteCommand.COMMAND_KEYWORD.length()).stripLeading();

        String pidString = matchPid(input);
        Integer pid = parseInteger(pidString);
        input = input.substring(pidString.length()).stripLeading();

        if (!input.isEmpty()) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_DELETE_FORMAT);
        }

        return new DeleteCommand(pid);
    }

    private EditCommand createEditCommand(String input)
            throws InvalidFormatException, UnsignedIntegerExceededException {
        Integer pid;
        String name = null;
        Integer quantity = null;
        Double price = null;
        String description = null;

        input = input.substring(EditCommand.COMMAND_KEYWORD.length()).stripLeading();

        String pidString = matchPid(input);
        pid = parseInteger(pidString);

        input = input.substring(pidString.length()).stripLeading();

        if (input.toLowerCase().startsWith(NAME_FLAG)) {
            String matchedName = matchString(NAME_PATTERN, input);
            validateName(matchedName);
            assert matchedName != null;
            name = matchedName.strip();
            input = input.substring(matchedName.length() + FLAG_LENGTH).stripLeading();
        }

        if (input.toLowerCase().startsWith(QUANTITY_FLAG)) {
            String matchedQuantity = matchQuantity(input);
            assert matchedQuantity != null;
            quantity = parseInteger(matchedQuantity.strip());
            input = input.substring(matchedQuantity.length() + FLAG_LENGTH).stripLeading();
        }

        if (input.toLowerCase().startsWith(PRICE_FLAG)) {
            String matchedPrice = matchPrice(input);
            assert matchedPrice != null;
            price = parsePrice(matchedPrice.strip());
            input = input.substring(matchedPrice.length() + FLAG_LENGTH).stripLeading();
        }

        if (input.toLowerCase().startsWith(DESCRIPTION_FLAG)) {
            String matchedDescription = matchString(DESCRIPTION_PATTERN, input);
            validateDescription(matchedDescription);
            assert matchedDescription != null;
            description = matchedDescription.strip();
            input = input.substring(matchedDescription.length() + FLAG_LENGTH);
        }

        if (!input.isEmpty()) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_EDIT_FORMAT);
        }

        return new EditCommand(pid, name, quantity, price, description);
    }

    private NewCommand createNewCommand(String input)
            throws InvalidFormatException, UnsignedIntegerExceededException {
        String name;
        Integer quantity;
        Double price = null;
        String description = null;

        input = input.substring(NewCommand.COMMAND_KEYWORD.length()).stripLeading();

        if (!input.toLowerCase().startsWith(NAME_FLAG)) {
            throw new InvalidFormatException(MESSAGE_ERROR_MISSING_NAME_FLAG);
        }
        String matchedName = matchString(NAME_PATTERN, input);
        validateName(matchedName);
        assert matchedName != null;
        name = matchedName.strip();
        input = input.substring(matchedName.length() + FLAG_LENGTH).stripLeading();


        if (!input.toLowerCase().startsWith(QUANTITY_FLAG)) {
            throw new InvalidFormatException(MESSAGE_ERROR_MISSING_QUANTITY_FLAG);
        }
        String matchedQuantity = matchQuantity(input);
        assert matchedQuantity != null;
        quantity = parseInteger(matchedQuantity.strip());
        input = input.substring(matchedQuantity.length() + FLAG_LENGTH).stripLeading();

        if (input.toLowerCase().startsWith(PRICE_FLAG)) {
            String matchedPrice = matchPrice(input);
            assert matchedPrice != null;
            price = parsePrice(matchedPrice.strip());
            input = input.substring(matchedPrice.length() + FLAG_LENGTH).stripLeading();
        }

        if (input.toLowerCase().startsWith(DESCRIPTION_FLAG)) {
            String matchedDescription = matchString(DESCRIPTION_PATTERN, input);
            validateDescription(matchedDescription);
            assert matchedDescription != null;
            description = matchedDescription.strip();
            input = input.substring(matchedDescription.length() + FLAG_LENGTH);
        }

        if (!input.isEmpty()) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_NEW_FORMAT);
        }

        return new NewCommand(name, quantity, price, description);
    }


    private FindCommand createFindCommand(String input) throws InvalidFormatException {
        input = input.substring(FindCommand.COMMAND_KEYWORD.length()).strip();

        String keyword = matchString(KEYWORD_PATTERN, input);
        validateFindKeyword(keyword);
        assert keyword != null;
        return new FindCommand(keyword);
    }

    private HistoryCommand createHistoryCommand(String input)
            throws InvalidFormatException, UnsignedIntegerExceededException {
        input = input.substring(HistoryCommand.COMMAND_KEYWORD.length()).strip();

        String pidString = matchPid(input);
        Integer pid = parseInteger(pidString);
        input = input.substring(pidString.length()).stripLeading();

        if (!input.isEmpty()) {
            throw new InvalidFormatException(MESSAGE_ERROR_INVALID_HISTORY_FORMAT);
        }

        return new HistoryCommand(pid);
    }

    private HelpCommand createHelpCommand(String input) throws InvalidFormatException {
        input = input.substring(HelpCommand.COMMAND_KEYWORD.length()).stripLeading();
        if (input.isEmpty()) {
            return new HelpCommand();
        }

        return new HelpCommand(input.toLowerCase());
    }

    /**
     * Parses the input string supplied by the user to the UI of StockPal into its respective command fields.
     *
     * @param input The input string to be parsed
     * @return A specific Command object containing parsed components of input
     * @throws InvalidFormatException If input string is not matched with its respective argument's regexes.
     * @throws UnsignedIntegerExceededException If parsed PID, quantity or dollars in price exceeds Integer.MAX_VALUE
     * @throws InvalidCommandException If input's first word (which is command word) is not a legal command word
     */
    public Command parseInput(String input)
            throws InvalidCommandException, InvalidFormatException, UnsignedIntegerExceededException {
        input = input.replaceAll("\\s", " ");
        input = input.stripLeading();
        String command = getCommandFromInput(input);

        switch (command) {
        case HelpCommand.COMMAND_KEYWORD:
            return createHelpCommand(input);

        case ListCommand.COMMAND_KEYWORD:
            return createListCommand(input);

        case ExitCommand.COMMAND_KEYWORD:
            return createExitCommand(input);

        case NewCommand.COMMAND_KEYWORD:
            return createNewCommand(input);

        case EditCommand.COMMAND_KEYWORD:
            return createEditCommand(input);

        case DeleteCommand.COMMAND_KEYWORD:
            return createDeleteCommand(input);

        case InflowCommand.COMMAND_KEYWORD:
            return createInflowCommand(input);

        case OutflowCommand.COMMAND_KEYWORD:
            return createOutflowCommand(input);

        case FindCommand.COMMAND_KEYWORD:
            return createFindCommand(input);

        case HistoryCommand.COMMAND_KEYWORD:
            return createHistoryCommand(input);

        default:
            LOGGER.log(Level.WARNING, MESSAGE_ERROR_INVALID_COMMAND);
            throw new InvalidCommandException(MESSAGE_ERROR_INVALID_COMMAND);
        }
    }
}
