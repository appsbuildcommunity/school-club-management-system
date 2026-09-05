package com.appsBuild.club_management_system.model.enums;

public enum Category {
    MANAGE_POSTS(false),
    MANAGE_EVENTS(false),
    MANAGE_MEMBERS(false),
    MANAGE_CLUBS(true);

    private final boolean privileged;

    Category(boolean privileged) {
        this.privileged = privileged;
    }

    public boolean isPrivileged() {
        return privileged;
    }
}
