package de.fantasypixel.rework.utils.modules;

public abstract class Service<C, R extends EntityRepo<?>> {

    private final C config;
    private final R entityRepo;

    public Service(C config, R entityRepo) {
        this.config = config;
        this.entityRepo = entityRepo;
    }

    public C getConfig() {
        return config;
    }

    public R getEntityRepo() {
        return entityRepo;
    }

}
