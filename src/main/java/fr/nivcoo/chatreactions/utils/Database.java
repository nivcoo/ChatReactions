package fr.nivcoo.chatreactions.utils;

import fr.nivcoo.utilsz.database.ColumnDefinition;
import fr.nivcoo.utilsz.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Database {
    private final DatabaseManager manager;

    public Database(DatabaseManager manager) {
        this.manager = manager;
    }

    public void initDB() {
        try {
            manager.createTable("ranking", Arrays.asList(
                    new ColumnDefinition("UUID", "TEXT", "PRIMARY KEY"),
                    new ColumnDefinition("count", "INTEGER", "DEFAULT 0")
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerScore(UUID uuid, int count) {
        try (Connection con = manager.getConnection();
             PreparedStatement ps = con.prepareStatement("REPLACE INTO ranking(UUID, count) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, count);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerScore(UUID uuid) {
        try (Connection con = manager.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT count FROM ranking WHERE UUID=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<UUID, Integer> getAllPlayersScore() {
        Map<UUID, Integer> all = new HashMap<>();
        try (Connection con = manager.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT UUID, count FROM ranking");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("UUID"));
                int count = rs.getInt("count");
                all.put(uuid, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return all;
    }

    public void clearDB() {
        try (Connection con = manager.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM ranking;")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
