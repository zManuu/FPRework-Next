package de.fantasypixel.rework.modules.account.options;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;

import javax.annotation.Nonnull;

@ServiceProvider
public class AccountOptionsService {

    private final static String CLASS_NAME = AccountOptionsService.class.getSimpleName();

    @Plugin private FPRework plugin;
    @DataRepo private DataRepoProvider<AccountOptions> dataRepo;
    @Config private AccountOptionsConfig config;

    /**
     * Creates a new account options entry in the database that uses the default options.
     * @param accountId the id of the associated account
     */
    private void createDefaultOptions(int accountId) {
        var defaultOptions = this.plugin.getGson().fromJson(
                this.plugin.getGson().toJson(this.config.getDefaultOptions()),
                AccountOptions.class
        );

        defaultOptions.setAccountId(accountId);

        if (this.dataRepo.insert(defaultOptions)) {
            this.plugin.getFpLogger().debug("Created default account-options for account {0}.", accountId);
        } else {
            this.plugin.getFpLogger().warning(CLASS_NAME, "createDefaultOptions", "Couldn't insert default account-options for account {0}.", accountId);
        }
    }

    /**
     * Retrieves the account options from the data-repository.
     * If none are found, the default options will be applied.
     * @param accountId the id of the associated account
     * @return the current account options
     */
    @Nonnull
    public AccountOptions getOptions(int accountId) {
        if (this.dataRepo.exists("accountId", accountId)) {
            var existingOptions = this.dataRepo.get("accountId", accountId);

            if (existingOptions == null) {
                this.plugin.getFpLogger().error(CLASS_NAME, "getOptions", "Tried to retrieve existing options for account {0}, but found none.", accountId);
                return this.config.getDefaultOptions();
            }

            return existingOptions;
        } else {
            this.createDefaultOptions(accountId);
            return this.config.getDefaultOptions();
        }
    }

    /**
     * Updates one option in the database.
     * The validation is done in this method.
     * @throws IllegalArgumentException if an option hasn't been found (-> no message) or if the given value was invalid (with message)
     * @return weather the update was successful
     */
    public boolean updateOptions(int accountId, String option, String value) throws IllegalArgumentException {
        var options = this.getOptions(accountId);

        if (option.equalsIgnoreCase("language") || option.equalsIgnoreCase("lang")) {
            if (!value.equalsIgnoreCase("de") && !value.equalsIgnoreCase("en"))
                throw new IllegalArgumentException("de, en");

            options.setLanguageKey(value);
        } else {
            // no matching option
            throw new IllegalArgumentException();
        }

        return this.dataRepo.update(options);
    }

    /**
     * @param accountId the id of the associated account
     * @return a text-representation of the account's options
     */
    @Nonnull
    public String[] getOptionsText(int accountId) {
        var options = this.getOptions(accountId);
        return new String[] {
                "Sprache: " + options.getLanguageKey()
        };
    }

    /**
     * @return a text-representation of all options and their possible values
     */
    public String[] getOptionDefinitionsText() {
        return new String[] {
                "language - de, en"
        };
    }

}
