/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.sadu.databases;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatementSplitterTest {

    @Test
    void testSimpleSplit() {

        final String SQL = "SELECT * FROM table; SELECT * FROM table2;";

        String[] splitStatements = StatementSplitter.split(SQL);

        assertEquals(2, splitStatements.length);
        assertEquals("SELECT * FROM table;", splitStatements[0].trim());
        assertEquals("SELECT * FROM table2;", splitStatements[1].trim());

    }

    @Test
    void testDelimiterSplit() {

        final String SQL = "SELECT * FROM table; DELIMITER $$ SELECT * FROM table2 $$ DELIMITER ; SELECT * FROM table3;";

        String [] splitStatements = StatementSplitter.split(SQL);

        assertEquals(3, splitStatements.length);
        assertEquals("SELECT * FROM table;", splitStatements[0].trim());
        assertEquals("DELIMITER $$ SELECT * FROM table2 $$ DELIMITER ;", splitStatements[1].replaceAll("\\s+", " "));
        assertEquals("SELECT * FROM table3;", splitStatements[2].trim());

    }

    @Test
    void testSplitOnStatementsWithoutSpaces() {

        final String SQL = "SELECT * FROM table;SELECT test FROM table;DELIMITER $$ SELECT * FROM table2 $$INSERT " +
                "INTO table VALUES(x,y)$$DELIMITER ; SELECT * FROM table3;";

        String[] splitStatements = StatementSplitter.split(SQL);

        assertEquals(7, splitStatements.length);
        assertEquals("SELECT * FROM table;", splitStatements[0].trim());
        assertEquals("SELECT test FROM table;", splitStatements[1].trim());
        assertEquals("DELIMITER $$", splitStatements[2].trim());
        assertEquals("SELECT * FROM table2 $$", splitStatements[3].trim());
        assertEquals("INSERT INTO table VALUES(x,y)$$", splitStatements[4].trim());
        assertEquals("DELIMITER ;", splitStatements[5].trim());
        assertEquals("SELECT * FROM table3;", splitStatements[6].trim());

    }

    @Test
    void testSplitOnTextBlock() {
        final String sql = """
                DELIMITER //
                CREATE FUNCTION IF NOT EXISTS is_prime (number INT) RETURNS BOOLEAN
                BEGIN
                    DECLARE i INT DEFAULT 2;
                    find_prime: WHILE i < number DO
                            IF number % i = 0 THEN
                                RETURN FALSE;
                            END IF;
                            SET i = i + 1;
                        END WHILE find_prime;
                    RETURN TRUE;
                END //
                DELIMITER ;
                                
                DELIMITER //
                CREATE FUNCTION IF NOT EXISTS next_prime_number(number INT) RETURNS INT
                BEGIN
                    DECLARE isPrime BOOLEAN DEFAULT FALSE;
                    DECLARE currentNumber INT DEFAULT number;
                    looping: WHILE isPrime = FALSE DO
                            SET currentNumber = currentNumber + 1;
                            IF is_prime(currentNumber) = TRUE THEN
                                RETURN currentNumber;
                            END IF;
                        END WHILE looping;
                END //
                DELIMITER ;
                """;

        String[] splitStatements = StatementSplitter.split(sql);

        assertEquals(2, splitStatements.length);
        assertEquals("DELIMITER // CREATE FUNCTION IF NOT EXISTS is_prime (number INT) RETURNS BOOLEAN BEGIN DECLARE i" +
                " INT DEFAULT 2; find_prime: WHILE i < number DO IF number % i = 0 THEN RETURN FALSE; END IF; SET i" +
                " = i + 1; END WHILE find_prime; RETURN TRUE; END // DELIMITER ;",
                splitStatements[0].trim().replaceAll(" +", " ")
                .replaceAll("\\s+", " "));
        assertEquals("DELIMITER // CREATE FUNCTION IF NOT EXISTS next_prime_number(number INT) RETURNS INT BEGIN DECLARE" +
                " isPrime BOOLEAN DEFAULT FALSE; DECLARE currentNumber INT DEFAULT number; looping: WHILE isPrime =" +
                " FALSE DO SET currentNumber = currentNumber + 1; IF is_prime(currentNumber) = TRUE THEN RETURN " +
                "currentNumber; END IF; END WHILE looping; END // DELIMITER ;",
                splitStatements[4].trim().replaceAll(" +", " ")
                        .replaceAll("\\s+", " "));

    }

    @Test
    void testSplitWithDelimiterString() {
        final String sql = """
                SELECT * FROM delimiter;
                DELIMITER //
                SELECT test FROM table2 //
                SELECT test FROM table //
                DELIMITER ;
                SELECT * FROM 'delimiter';
                """;

        String[] splitStatements = StatementSplitter.split(sql);

        assertEquals(4, splitStatements.length);
        assertEquals("SELECT * FROM delimiter;", splitStatements[0].trim().replaceAll(" +", " ")
                .replaceAll("\\s+", " "));
        assertEquals("DELIMITER // SELECT delimiter FROM table2 // DELIMITER ;", splitStatements[2].trim().replaceAll(" +", " ")
                .replaceAll("\\s+", " "));
        assertEquals("DELIMITER // SELECT test FROM table // DELIMITER ;", splitStatements[2].trim().replaceAll(" +", " ")
                .replaceAll("\\s+", " "));
        assertEquals("SELECT * FROM 'delimiter';", splitStatements[3].trim().replaceAll(" +", " ")
                .replaceAll("\\s+", " "));
    }
}
