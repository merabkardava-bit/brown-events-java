package com.brownevents.app.dto.mapper;

import com.brownevents.app.dto.RoomResponse;
import com.brownevents.app.entity.Room;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static RoomResponse toResponse(Room room) {
        if (room == null) {
            return null;
        }
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getLocation()
        );
    }
}
