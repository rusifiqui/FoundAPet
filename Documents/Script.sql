-- TABLA DE USUARIOS
CREATE TABLE USERS (ID_USER INTEGER PRIMARY KEY AUTO_INCREMENT,
            USERNAME TEXT NOT NULL,
            PASSWORD TEXT NOT NULL,
            FIRST_NAME TEXT NOT NULL,
			LAST_NAME TEXT NOT NULL,
			EMAIL TEXT NOT NULL,
			LATITUDE TEXT NOT NULL,
            LONGITUDE TEXT NOT NULL,
            STATE TEXT NOT NULL,
            PATROL_STATE BOOLEAN NOT NULL DEFAULT FALSE,
            ACTIVE BOOLEAN NOT NULL DEFAULT TRUE);


-- TABLA DE MASCOTAS PERDIDAS
CREATE TABLE PETS_DATA (ID_PET INTEGER PRIMARY KEY AUTO_INCREMENT,
            TYPE TEXT NOT NULL,
            RACE TEXT NOT NULL,
            LATITUDE TEXT NOT NULL,
            LONGITUDE TEXT NOT NULL,
            IMG MEDIUMBLOB,
            STATE TEXT NOT NULL,
            DESCRIPTION TEXT NOT NULL,
            STATUS INTEGER, -- 0 -> LOST, 1 -> FOUND
            ID_USER INTEGER NOT NULL,
            FOUND_OR_LOST BOOLEAN NOT NULL DEFAULT FALSE); -- false -> FOUND, true --> LOST

-- TABLA DE COMENTARIOS
CREATE TABLE COMMENTS (ID_COMMENT INTEGER PRIMARY KEY AUTO_INCREMENT,
            COMMENT TEXT NOT NULL,
            COMMENT_DATE DATE NOT NULL,
            ID_USER INTEGER NOT NULL,
            USER_NAME TEXT NOT NULL,
            ID_PET INTEGER NOT NULL);

-- TABLA DE MENSAJES
CREATE TABLE MESSAGES (ID_MESSAGE INTEGER PRIMARY KEY AUTO_INCREMENT,
            MESSAGE TEXT NOT NULL,
            ACTIVE INTEGER NOT NULL);