CREATE SEQUENCE INT_MESSAGE_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE;

CREATE TABLE INT_CHANNEL_MESSAGE (
                                     MESSAGE_ID CHAR(36) NOT NULL,
                                     GROUP_KEY CHAR(36) NOT NULL,
                                     CREATED_DATE BIGINT NOT NULL,
                                     MESSAGE_PRIORITY BIGINT,
                                     MESSAGE_SEQUENCE BIGINT NOT NULL DEFAULT nextval('INT_MESSAGE_SEQ'),
                                     MESSAGE_BYTES BYTEA,
                                     REGION VARCHAR(100) NOT NULL,
                                     CONSTRAINT INT_CHANNEL_MESSAGE_PK PRIMARY KEY (REGION, GROUP_KEY, CREATED_DATE, MESSAGE_SEQUENCE)
);

CREATE INDEX INT_CHANNEL_MSG_DELETE_IDX
    ON INT_CHANNEL_MESSAGE (REGION, GROUP_KEY, MESSAGE_ID);
