--
-- PostgreSQL database dump
--

-- Dumped from database version 11.5
-- Dumped by pg_dump version 11.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: db-connection-test; Type: TABLE; Schema: public; Owner: aristotle
--

CREATE TABLE public."db-connection-test" (
    id character varying(255)
);


ALTER TABLE public."db-connection-test" OWNER TO aristotle;

--
-- Name: indexed-videos; Type: TABLE; Schema: public; Owner: aristotle
--

CREATE TABLE public."indexed-videos" (
    id character varying(32),
    "date-indexed" date,
    "last-updated" date,
    "instance-username" character varying(255)
);


ALTER TABLE public."indexed-videos" OWNER TO aristotle;

--
-- Name: instances; Type: TABLE; Schema: public; Owner: aristotle
--

CREATE TABLE public.instances (
    username character varying(255),
    name character varying(255),
    "backButtonURL" character varying(255),
    "backButtonText" character varying(255),
    "searchBarText" character varying(255),
    "videoSolrConfigID" integer,
    "blockSolrConfigID" integer,
    active boolean
);


ALTER TABLE public.instances OWNER TO aristotle;

--
-- Name: solr-configs; Type: TABLE; Schema: public; Owner: aristotle
--

CREATE TABLE public."solr-configs" (
    id integer NOT NULL,
    host character varying(255),
    core character varying(255),
    port integer,
    ssl boolean
);


ALTER TABLE public."solr-configs" OWNER TO aristotle;

--
-- Name: solr-configs_id_seq; Type: SEQUENCE; Schema: public; Owner: aristotle
--

CREATE SEQUENCE public."solr-configs_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."solr-configs_id_seq" OWNER TO aristotle;

--
-- Name: solr-configs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: aristotle
--

ALTER SEQUENCE public."solr-configs_id_seq" OWNED BY public."solr-configs".id;


--
-- Name: youtube-urls; Type: TABLE; Schema: public; Owner: aristotle
--

CREATE TABLE public."youtube-urls" (
    "instance-username" character varying(255),
    id character varying(255),
    "id-type" character varying(128)
);


ALTER TABLE public."youtube-urls" OWNER TO aristotle;

--
-- Name: solr-configs id; Type: DEFAULT; Schema: public; Owner: aristotle
--

ALTER TABLE ONLY public."solr-configs" ALTER COLUMN id SET DEFAULT nextval('public."solr-configs_id_seq"'::regclass);


--
-- Data for Name: db-connection-test; Type: TABLE DATA; Schema: public; Owner: aristotle
--

COPY public."db-connection-test" (id) FROM stdin;
foo
bar
baz
\.

--
-- Data for Name: solr-configs; Type: TABLE DATA; Schema: public; Owner: aristotle
--

COPY public."solr-configs" (id, host, core, port, ssl) FROM stdin;
4	test	block	6969	t
5	test	video	6969	t
11	solr.vlogbox.me	videos	8983	f
12	solr.vlogbox.me	video_blocks	8983	f
23	localhost	videos	8983	f
24	localhost	blocks	8983	f
\.


--
-- Name: solr-configs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: aristotle
--

SELECT pg_catalog.setval('public."solr-configs_id_seq"', 42, true);


--
-- Name: solr-configs solr-configs_pkey; Type: CONSTRAINT; Schema: public; Owner: aristotle
--

ALTER TABLE ONLY public."solr-configs"
    ADD CONSTRAINT "solr-configs_pkey" PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

