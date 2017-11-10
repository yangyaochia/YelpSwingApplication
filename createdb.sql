-- create Business table
CREATE TABLE Business (
    business_id     VARCHAR(100) NOT NULL,
    city            VARCHAR(50),
    state           VARCHAR(30),
    name            VARCHAR(150) NOT NULL,
    full_address    VARCHAR(200),
    stars           NUMBER,
    number_checkin  INTEGER,
    PRIMARY KEY (business_id)
);
-- create Business open hour table
CREATE TABLE OpenHour (
    business_id VARCHAR(100),
    day         VARCHAR(10),
    from_time   VARCHAR(10),
    to_time     VARCHAR(10),
    PRIMARY KEY (business_id, day, from_time, to_time),
    FOREIGN KEY (business_id) 
    REFERENCES Business
    ON DELETE CASCADE                 
);

-- create Business category table
CREATE TABLE MainCategory (
    business_id    VARCHAR(100),
    mainCategory   VARCHAR(150),
    PRIMARY KEY (business_id, mainCategory)
    ,
    --CONSTRAINT main_c_b
    FOREIGN KEY (business_id) 
    REFERENCES Business
    ON DELETE CASCADE
);
-- create Business subcategory table
CREATE TABLE SubCategory (
    business_id     VARCHAR(100),
    subCategory     VARCHAR(150),
    PRIMARY KEY (business_id, subCategory)
    ,
    --CONSTRAINT sub_c_b
    FOREIGN KEY (business_id) 
    REFERENCES Business
    ON DELETE CASCADE
);
-- create Attribute table
CREATE TABLE Attribute (
    business_id     VARCHAR(100),
    attribute       VARCHAR(200),
    PRIMARY KEY (business_id, attribute),
    --CONSTRAINT attri_b
    FOREIGN KEY (business_id) 
    REFERENCES Business
    ON DELETE CASCADE
);
-- create YelpUser table
CREATE TABLE YelpUser (
    user_id         VARCHAR(50),
    name            VARCHAR(150) NOT NULL,
    yelping_since   VARCHAR(20),
    review_count    INTEGER,
    average_stars   NUMBER,
    friend_count    INTEGER,
    votes           INTEGER,
    PRIMARY KEY (user_id)
);
-- create Review table
CREATE TABLE Review (
    review_id       VARCHAR(100),
    business_id     VARCHAR(100) NOT NULL,
    user_id         VARCHAR(100) NOT NULL,
    review_date     VARCHAR(20),
    review_text     CLOB,
    stars           INTEGER,
    votes           INTEGER,
    PRIMARY KEY (review_id)
    ,
    --CONSTRAINT business_review
    FOREIGN KEY (business_id) 
    REFERENCES Business
    ON DELETE CASCADE
    ,
    --CONSTRAINT review_author
    FOREIGN KEY (user_id) 
    REFERENCES YelpUser
    ON DELETE CASCADE
);
-- create index for Open table
CREATE INDEX INDEX_OPEN_DAY ON OpenHour (day);
CREATE INDEX INDEX_OPEN_FROM ON OpenHour (from_time);
CREATE INDEX INDEX_OPEN_TO ON OpenHour (to_time);
-- create index for MainCategory table
CREATE INDEX INDEX_MAINCATEGORY_NAME ON MainCategory (mainCategory);
-- create index for SubCategory table
CREATE INDEX INDEX_SUBCATEGORY_NAME ON SubCategory (subCategory);
-- create index for Attribute table
CREATE INDEX INDEX_ATTRIBUTE ON Attribute (attribute);
-- create index for YelpUser table
CREATE INDEX INDEX_USER_NAME ON YelpUser (name);
--CREATE INDEX INDEX_USER_REVIEWCOUNT ON YelpUser (review_count);
--CREATE INDEX INDEX_USER_VOTE ON YelpUser (votes);
--CREATE INDEX INDEX_USER_STAR ON YelpUser (average_stars);
--CREATE INDEX INDEX_USER_FRIENDS ON YelpUser (friend_count);
-- create index for review table
CREATE INDEX INDEX_REVIEW_DATE ON REVIEW (review_date);
CREATE INDEX INDEX_REVIEW_ID ON REVIEW (business_id, review_id);
CREATE INDEX INDEX_REVIEW_BID ON REVIEW (business_id);
CREATE INDEX INDEX_REVIEW_STAR ON REVIEW (stars);
CREATE INDEX INDEX_REVIEW_VOTE ON REVIEW (votes);
-- create index for business table
CREATE INDEX INDEX_BUSINESS_CITY ON BUSINESS (city);
CREATE INDEX INDEX_BUSINESS_STATE ON BUSINESS (state);
CREATE INDEX INDEX_BUSINESS_STARS ON BUSINESS (stars);
CREATE INDEX INDEX_BUSINESS_NUMCHECKINS ON BUSINESS (number_checkin);
