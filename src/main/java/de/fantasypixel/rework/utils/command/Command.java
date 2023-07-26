package de.fantasypixel.rework.utils.command;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    String name();
    String permission() default "";
    String[] aliases() default {};
    CommandTarget target() default CommandTarget.PLAYER;
}
