package apple.discord.acd.slash.base;

import apple.discord.acd.ACD;
import apple.discord.acd.slash.ACDSlashCommandHandler;
import apple.discord.acd.slash.group.ACDSlashSubGroupCommand;
import apple.discord.acd.slash.runner.ACDSlashMethodCommand;
import apple.discord.acd.slash.runner.SlashRunner;
import apple.discord.acd.slash.sub.ACDSlashSubCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ACDSlashCommand implements ACDSlashCommandHandler {
    private final String alias;
    private final String description;
    private final Collection<ACDSlashSubGroupCommand> subGroups = new ArrayList<>();
    private final Collection<ACDSlashSubCommand> subCommands;
    private final SlashRunner runner;
    private final boolean isPermissionRequired;

    public ACDSlashCommand(ACD acd) {
        ACDBaseCommand annotation = getClass().getAnnotation(ACDBaseCommand.class);
        String path = this.alias = annotation.alias();
        this.description = annotation.description();
        this.isPermissionRequired = annotation.isPermissionRequired();
        Class<?>[] innerClasses = getClass().getClasses();
        for (Class<?> innerClass : innerClasses) {
            if (ACDSlashSubGroupCommand.class.isAssignableFrom(innerClass)) {
                try {
                    @SuppressWarnings("unchecked") Class<? extends ACDSlashSubGroupCommand> castedInnerClass = (Class<? extends ACDSlashSubGroupCommand>) innerClass;
                    subGroups.add(castedInnerClass.getDeclaredConstructor(getClass(), String.class, ACD.class, ACDSlashCommandHandler.class).newInstance(this, path, acd, this));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    acd.doInitializerException(e);
                }
            }
        }

        this.runner = getRunner(this, getClass(), acd, path);

        // register all sub-classes that are of type ACDSlashSubGroupCommand
        this.subCommands = getSubCommands(this, this.getClass(), path, acd);
        acd.addCommand(this);
    }

    public static SlashRunner getRunner(ACDSlashCommandHandler thingOfSuper, Class<?> classOfSuper, ACD acd, String path) {
        Method[] methods = classOfSuper.getDeclaredMethods();
        for (Method method : methods) {
            ACDSlashMethodCommand methodAnnotation = method.getAnnotation(ACDSlashMethodCommand.class);
            if (methodAnnotation != null) {
                return new SlashRunner(thingOfSuper, method, methodAnnotation, acd, path);
            }
        }
        return null;
    }


    public static Collection<ACDSlashSubCommand> getSubCommands(ACDSlashCommandHandler thingOfSuper /*no idea how this works*/, Class<?> classOfSuper, String path, ACD acd) {
        Collection<ACDSlashSubCommand> subCommands = new ArrayList<>();
        Class<?>[] innerClasses = classOfSuper.getClasses();
        for (Class<?> innerClass : innerClasses) {
            if (ACDSlashSubCommand.class.isAssignableFrom(innerClass)) {
                try {
                    @SuppressWarnings("unchecked") Class<? extends ACDSlashSubCommand> castedInnerClass = (Class<? extends ACDSlashSubCommand>) innerClass;
                    subCommands.add(castedInnerClass.getDeclaredConstructor(classOfSuper, String.class, ACD.class, ACDSlashCommandHandler.class).newInstance(thingOfSuper, path, acd, thingOfSuper));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    acd.doInitializerException(e);
                }
            }
        }
        return subCommands;
    }

    public CommandData getCommand() {
        CommandData command = new CommandData(alias, description);
        // add subGroupCommands
        Collection<SubcommandGroupData> subGroupsCommandData = new ArrayList<>();
        for (ACDSlashSubGroupCommand subGroup : this.subGroups) {
            subGroupsCommandData.add(subGroup.getCommand());
        }
        if (!subGroupsCommandData.isEmpty())
            command.addSubcommandGroups(subGroupsCommandData);

        // add subCommands
        Collection<SubcommandData> subcommandsData = new ArrayList<>();
        for (ACDSlashSubCommand subCommand : this.subCommands) {
            subcommandsData.add(subCommand.getCommand());
        }
        if (!subcommandsData.isEmpty())
            command.addSubcommands(subcommandsData);

        // set the permissions of this
        command.setDefaultEnabled(!isPermissionRequired);

        // add options (methods define this)
        if (runner != null)
            command.addOptions(runner.getOptionsData());
        return command;
    }

    public Collection<SlashRunner> getRunners() {
        List<SlashRunner> runners = new ArrayList<>();
        for (ACDSlashSubGroupCommand subGroup : subGroups) {
            runners.addAll(subGroup.getRunners());
        }
        for (ACDSlashSubCommand subCommand : subCommands) {
            runners.addAll(subCommand.getRunners());
        }
        if (this.runner != null) runners.add(this.runner);
        return runners;
    }
}
