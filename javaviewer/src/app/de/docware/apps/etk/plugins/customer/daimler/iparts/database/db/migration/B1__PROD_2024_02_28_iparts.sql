--
-- PostgreSQL database dump
--

-- Dumped from database version 15.5
-- Dumped by pg_dump version 16.1

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

--
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- Name: pgaudit; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgaudit WITH SCHEMA public;


--
-- Name: EXTENSION pgaudit; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pgaudit IS 'provides auditing functionality';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: best_h; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.best_h (
    b_satzid character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_user character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_bdatum character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf1 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf2 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf3 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf4 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf5 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf6 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf7 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf8 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf9 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf10 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf11 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf12 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf13 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf14 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf15 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf16 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf17 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf18 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf19 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empf20 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs1 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs2 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs3 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs4 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs5 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs6 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs7 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs8 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs9 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs10 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs11 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs12 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs13 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs14 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs15 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs16 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs17 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs18 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs19 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_abs20 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech1 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech2 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech3 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech4 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech5 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech6 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech7 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech8 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech9 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech10 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech11 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech12 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech13 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech14 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech15 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech16 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech17 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech18 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech19 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rech20 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief1 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief2 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief3 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief4 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief5 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief6 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief7 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief8 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief9 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief10 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief11 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief12 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief13 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief14 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief15 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief16 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief17 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief18 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief19 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lief20 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_datum character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_betreff character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_art character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_versand character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_bemerk character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_empfadrid character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_rechadrid character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_absadrid character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_liefadrid character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: bestell; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bestell (
    b_satzid character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_user character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_kvari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_kver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_klfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_itemtype character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_itemid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_partver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_price character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_wkz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_adddata text COLLATE pg_catalog."en-US-x-icu",
    b_typ character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_manpart character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_updatedfromplugin character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_path text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld1 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld2 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld3 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld4 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld5 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld6 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld7 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld8 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld9 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld10 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld11 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld12 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld13 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld14 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld15 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld16 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld17 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld18 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld19 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_feld20 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: custprop; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.custprop (
    c_kvari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    c_kver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    c_klfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    c_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    c_mver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    c_key character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    c_sprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    c_text text COLLATE pg_catalog."en-US-x-icu",
    c_datatype character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    c_data bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_ac_pc_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_ac_pc_mapping (
    dapm_assortment_class character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dapm_as_product_class character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_ac_pc_permission_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_ac_pc_permission_mapping (
    dppm_brand character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dppm_assortment_class character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dppm_as_product_class character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_acc_codes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_acc_codes (
    dacc_code character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_agg_part_codes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_agg_part_codes (
    dapc_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dapc_code character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dapc_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dapc_factory character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dapc_factory_sign character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dapc_date_from character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dapc_date_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_aggs_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_aggs_mapping (
    dam_dialog_agg_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dam_mad_agg_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_ao_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_ao_history (
    dah_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dah_change_date character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dah_change_user_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dah_action character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dah_seqno character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_ao_history ALTER COLUMN dah_change_date SET STATISTICS 250;


--
-- Name: da_as_codes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_as_codes (
    das_code character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_author_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_author_order (
    dao_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_name character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_desc text COLLATE pg_catalog."en-US-x-icu",
    dao_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_creation_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_creation_user_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_change_set_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_current_user_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_creator_grp_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_current_grp_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_bst_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_bst_supplied character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dao_bst_error text COLLATE pg_catalog."en-US-x-icu",
    dao_reldate character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_bad_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_bad_code (
    dbc_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbc_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbc_code_id character varying(390) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbc_expiry_date character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbc_permanent_bad_code character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_bom_mat_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_bom_mat_history (
    dbmh_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbmh_part_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbmh_rev_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbmh_rev_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbmh_kem_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbmh_kem_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbmh_release_from character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbmh_release_to character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_bom_mat_history ALTER COLUMN dbmh_part_no SET STATISTICS 500;
ALTER TABLE ONLY public.da_bom_mat_history ALTER COLUMN dbmh_kem_from SET STATISTICS 500;
ALTER TABLE ONLY public.da_bom_mat_history ALTER COLUMN dbmh_kem_to SET STATISTICS 500;
ALTER TABLE ONLY public.da_bom_mat_history ALTER COLUMN dbmh_release_from SET STATISTICS 500;
ALTER TABLE ONLY public.da_bom_mat_history ALTER COLUMN dbmh_release_to SET STATISTICS 500;


--
-- Name: da_branch_pc_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_branch_pc_mapping (
    dbm_branch character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dbm_as_product_classes text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_change_set; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_change_set (
    dcs_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcs_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcs_commit_date character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcs_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_change_set ALTER COLUMN dcs_guid SET STATISTICS 250;
ALTER TABLE ONLY public.da_change_set ALTER COLUMN dcs_commit_date SET STATISTICS 250;


--
-- Name: da_change_set_entry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_change_set_entry (
    dce_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dce_do_type character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dce_do_id character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dce_do_id_old character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dce_current_data bytea,
    dce_history_data bytea,
    dce_do_source_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dce_edit_info character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dce_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_change_set_entry ALTER COLUMN dce_guid SET STATISTICS 250;
ALTER TABLE ONLY public.da_change_set_entry ALTER COLUMN dce_do_id SET STATISTICS 1000;
ALTER TABLE ONLY public.da_change_set_entry ALTER COLUMN dce_do_source_guid SET STATISTICS 500;
ALTER TABLE ONLY public.da_change_set_entry ALTER COLUMN dce_matnr SET STATISTICS 250;


--
-- Name: da_change_set_info_defs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_change_set_info_defs (
    dcid_do_type character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcid_feld character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcid_as_relevant character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcid_mustfield character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_code (
    dc_code_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dc_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dc_pgrp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dc_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dc_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dc_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dc_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_code ALTER COLUMN dc_desc SET STATISTICS 250;


--
-- Name: da_code_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_code_mapping (
    dcm_category character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcm_model_type_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcm_initial_code character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcm_target_code character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_color_number; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_color_number (
    dcn_color_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcn_sda character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcn_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcn_edat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcn_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcn_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_colortable_content; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_colortable_content (
    dctc_table_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_pos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_color_var character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_pgrp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_code text COLLATE pg_catalog."en-US-x-icu",
    dctc_etkz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_code_as text COLLATE pg_catalog."en-US-x-icu",
    dctc_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_eval_pem_from character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_eval_pem_to character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_event_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_event_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_event_from_as character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctc_event_to_as character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_colortable_content ALTER COLUMN dctc_table_id SET STATISTICS 250;


--
-- Name: da_colortable_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_colortable_data (
    dctd_table_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctd_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctd_bem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctd_fikz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctd_valid_series character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctd_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_colortable_data ALTER COLUMN dctd_table_id SET STATISTICS 250;


--
-- Name: da_colortable_factory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_colortable_factory (
    dccf_table_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_pos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_factory character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_data_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_pema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_pemb character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_pemta character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_pemtb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_stca character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_stcb character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_pos_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_event_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_event_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_original_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dccf_is_deleted character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_colortable_factory ALTER COLUMN dccf_pos SET STATISTICS 250;
ALTER TABLE ONLY public.da_colortable_factory ALTER COLUMN dccf_adat SET STATISTICS 500;


--
-- Name: da_colortable_part; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_colortable_part (
    dctp_table_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctp_pos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctp_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctp_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctp_part character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctp_etkz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctp_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctp_pos_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctp_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctp_eval_pem_from character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dctp_eval_pem_to character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_colortable_part ALTER COLUMN dctp_table_id SET STATISTICS 250;
ALTER TABLE ONLY public.da_colortable_part ALTER COLUMN dctp_pos SET STATISTICS 250;
ALTER TABLE ONLY public.da_colortable_part ALTER COLUMN dctp_part SET STATISTICS 250;


--
-- Name: da_comb_text; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_comb_text (
    dct_module character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dct_modver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dct_seqno character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dct_text_seqno character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dct_dict_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dct_text_neutral character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dct_source_genvo character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_comb_text ALTER COLUMN dct_module SET STATISTICS 250;
ALTER TABLE ONLY public.da_comb_text ALTER COLUMN dct_dict_text SET STATISTICS 250;


--
-- Name: da_confirm_changes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_confirm_changes (
    dcc_change_set_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcc_do_type character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcc_do_id character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcc_partlist_entry_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcc_do_source_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcc_confirmation_user character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcc_confirmation_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_const_kit_content; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_const_kit_content (
    dckc_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dckc_pose character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dckc_ww character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dckc_sda character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dckc_sdb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dckc_sub_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dckc_kem_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dckc_kem_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dckc_quantity character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dckc_source_key character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dckc_proposed_source_type character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_const_kit_content ALTER COLUMN dckc_part_no SET STATISTICS 250;


--
-- Name: da_const_status_codes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_const_status_codes (
    dasc_code character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_cortex_import_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_cortex_import_data (
    dci_creation_ts character varying(25) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dci_endpoint_name character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dci_import_method character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dci_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dci_data bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_country_code_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_country_code_mapping (
    dcm_region_code character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcm_country_codes text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_country_invalid_parts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_country_invalid_parts (
    dcip_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcip_country_code character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_country_valid_series; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_country_valid_series (
    dcvs_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcvs_country_code character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_dialog; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dialog (
    dd_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_etkz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_pose character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_posv character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_ww character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_hierarchy character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_etz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_codes text COLLATE pg_catalog."en-US-x-icu",
    dd_steering character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_quantity_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_rfg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_kema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_kemb character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_steua character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_steub character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_product_grp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_sesi character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_posp character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_fed character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_rfmea character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_rfmen character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_bza character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_pte character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_kgum character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_distr text COLLATE pg_catalog."en-US-x-icu",
    dd_zflag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_varg character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_varm character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_ges character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_proj character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_code_len character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_bzae_neu text COLLATE pg_catalog."en-US-x-icu",
    dd_docu_relevant character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_event_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_event_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_linked_factory_data_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_dialog ALTER COLUMN dd_guid SET STATISTICS 1000;
ALTER TABLE ONLY public.da_dialog ALTER COLUMN dd_partno SET STATISTICS 500;
ALTER TABLE ONLY public.da_dialog ALTER COLUMN dd_kema SET STATISTICS 500;
ALTER TABLE ONLY public.da_dialog ALTER COLUMN dd_kemb SET STATISTICS 250;
ALTER TABLE ONLY public.da_dialog ALTER COLUMN dd_sdata SET STATISTICS 500;
ALTER TABLE ONLY public.da_dialog ALTER COLUMN dd_sdatb SET STATISTICS 250;


--
-- Name: da_dialog_add_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dialog_add_data (
    dad_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_pose character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_posv character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_ww character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_etz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_add_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_text_neutral character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_hierarchy character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_code text COLLATE pg_catalog."en-US-x-icu",
    dad_internal_text character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_event_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dad_event_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_dialog_add_data ALTER COLUMN dad_guid SET STATISTICS 500;
ALTER TABLE ONLY public.da_dialog_add_data ALTER COLUMN dad_adat SET STATISTICS 500;
ALTER TABLE ONLY public.da_dialog_add_data ALTER COLUMN dad_sdata SET STATISTICS 250;
ALTER TABLE ONLY public.da_dialog_add_data ALTER COLUMN dad_sdatb SET STATISTICS 250;


--
-- Name: da_dialog_changes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dialog_changes (
    ddc_do_type character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ddc_do_id character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ddc_hash character varying(40) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ddc_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ddc_bcte character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ddc_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ddc_change_set_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ddc_katalog_id character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_dialog_dsr; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dialog_dsr (
    dsr_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_type character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_no character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_sdata character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_sdatb character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_mk1 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_mk2 character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_mk3 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_mk4 character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_mk5 character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_mk6 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_mk7 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_mk_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsr_mk_id character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_dialog_dsr ALTER COLUMN dsr_matnr SET STATISTICS 250;
ALTER TABLE ONLY public.da_dialog_dsr ALTER COLUMN dsr_mk1 SET STATISTICS 500;
ALTER TABLE ONLY public.da_dialog_dsr ALTER COLUMN dsr_mk3 SET STATISTICS 500;
ALTER TABLE ONLY public.da_dialog_dsr ALTER COLUMN dsr_mk6 SET STATISTICS 250;
ALTER TABLE ONLY public.da_dialog_dsr ALTER COLUMN dsr_mk7 SET STATISTICS 250;
ALTER TABLE ONLY public.da_dialog_dsr ALTER COLUMN dsr_mk_text SET STATISTICS 250;


--
-- Name: da_dialog_partlist_text; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dialog_partlist_text (
    dd_plt_br character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_pose character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_posv character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_ww character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_etz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_textkind character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_pg character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_fed character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_aatab character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_str character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_plt_rfg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_dialog_partlist_text ALTER COLUMN dd_plt_sdata SET STATISTICS 250;
ALTER TABLE ONLY public.da_dialog_partlist_text ALTER COLUMN dd_plt_sdatb SET STATISTICS 250;
ALTER TABLE ONLY public.da_dialog_partlist_text ALTER COLUMN dd_plt_text SET STATISTICS 500;


--
-- Name: da_dialog_pos_text; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dialog_pos_text (
    dd_pos_br character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_pos_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_pos_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_pos_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_pos_pos character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_pos_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_pos_sesi character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_pos_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dd_pos_textnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_dialog_pos_text ALTER COLUMN dd_pos_sdata SET STATISTICS 250;
ALTER TABLE ONLY public.da_dialog_pos_text ALTER COLUMN dd_pos_sdatb SET STATISTICS 250;
ALTER TABLE ONLY public.da_dialog_pos_text ALTER COLUMN dd_pos_textnr SET STATISTICS 500;


--
-- Name: da_dict_meta; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dict_meta (
    da_dict_meta_txtkind_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_meta_textid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_meta_foreignid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_meta_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_meta_state character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_meta_create character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_meta_change character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_meta_userid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_meta_dialogid character varying(400) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_meta_eldasid character varying(400) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_dict_meta ALTER COLUMN da_dict_meta_textid SET STATISTICS 500;
ALTER TABLE ONLY public.da_dict_meta ALTER COLUMN da_dict_meta_foreignid SET STATISTICS 500;
ALTER TABLE ONLY public.da_dict_meta ALTER COLUMN da_dict_meta_create SET STATISTICS 250;
ALTER TABLE ONLY public.da_dict_meta ALTER COLUMN da_dict_meta_eldasid SET STATISTICS 250;


--
-- Name: da_dict_sprache; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dict_sprache (
    da_dict_sprache_textid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_sprache_sprach character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_sprache_create character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_sprache_change character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_sprache_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_sprache_trans_jobid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_sprache_trans_state character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_dict_sprache ALTER COLUMN da_dict_sprache_textid SET STATISTICS 500;
ALTER TABLE ONLY public.da_dict_sprache ALTER COLUMN da_dict_sprache_create SET STATISTICS 250;
ALTER TABLE ONLY public.da_dict_sprache ALTER COLUMN da_dict_sprache_change SET STATISTICS 250;


--
-- Name: da_dict_trans_job; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dict_trans_job (
    dtj_textid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_jobid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_source_lang character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_dest_lang character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_translation_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_bundle_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_translation_state character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_state_change character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_last_modified character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_job_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_textkind character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_user_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtj_error_code character varying(400) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_dict_trans_job_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dict_trans_job_history (
    dtjh_textid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_jobid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_source_lang character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_dest_lang character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_last_modified character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_translation_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_bundle_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_translation_state character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_state_change character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_job_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_textkind character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_user_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtjh_error_code character varying(400) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_dict_txtkind; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dict_txtkind (
    da_dict_tk_txtkind_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_tk_name character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_tk_length character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_tk_neutral character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_tk_change character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_tk_userid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_tk_foreign_tkind character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_tk_transit_tkind character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_dict_txtkind_usage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_dict_txtkind_usage (
    da_dict_tku_txtkind_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_tku_feld character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_tku_change character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dict_tku_userid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_eds_const_kit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_eds_const_kit (
    dck_snr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_partpos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_revfrom character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_revto character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_kemfrom character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_kemto character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_release_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_release_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_sub_snr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_note_id character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_wwkb character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_steering character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_quantity_flag character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_rfg character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_factory_ids character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_replenishment_kind character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_transmission_kit character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dck_wwzm character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_eds_const_kit ALTER COLUMN dck_snr SET STATISTICS 250;
ALTER TABLE ONLY public.da_eds_const_kit ALTER COLUMN dck_kemfrom SET STATISTICS 250;
ALTER TABLE ONLY public.da_eds_const_kit ALTER COLUMN dck_kemto SET STATISTICS 250;
ALTER TABLE ONLY public.da_eds_const_kit ALTER COLUMN dck_release_from SET STATISTICS 250;
ALTER TABLE ONLY public.da_eds_const_kit ALTER COLUMN dck_release_to SET STATISTICS 250;
ALTER TABLE ONLY public.da_eds_const_kit ALTER COLUMN dck_guid SET STATISTICS 500;
ALTER TABLE ONLY public.da_eds_const_kit ALTER COLUMN dck_sub_snr SET STATISTICS 500;


--
-- Name: da_eds_const_props; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_eds_const_props (
    dcp_snr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcp_partpos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcp_btx_flag character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcp_revfrom character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcp_revto character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcp_kemfrom character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcp_kemto character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcp_release_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcp_release_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dcp_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_eds_const_props ALTER COLUMN dcp_snr SET STATISTICS 250;
ALTER TABLE ONLY public.da_eds_const_props ALTER COLUMN dcp_text SET STATISTICS 500;


--
-- Name: da_eds_mat_remarks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_eds_mat_remarks (
    demr_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    demr_rev_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    demr_remark_no character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    demr_remark character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    demr_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_eds_mat_ww_flags; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_eds_mat_ww_flags (
    demw_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    demw_rev_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    demw_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    demw_text character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_eds_model; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_eds_model (
    eds_modelno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_group character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_scope character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_pos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_steering character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_aa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_revfrom character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_revto character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_kemfrom character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_kemto character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_msaakey character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_rfg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_quantity character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_pgkz character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_code text COLLATE pg_catalog."en-US-x-icu",
    eds_factories character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_release_from character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eds_release_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_eds_model ALTER COLUMN eds_kemfrom SET STATISTICS 250;
ALTER TABLE ONLY public.da_eds_model ALTER COLUMN eds_kemto SET STATISTICS 250;
ALTER TABLE ONLY public.da_eds_model ALTER COLUMN eds_msaakey SET STATISTICS 250;
ALTER TABLE ONLY public.da_eds_model ALTER COLUMN eds_release_from SET STATISTICS 250;
ALTER TABLE ONLY public.da_eds_model ALTER COLUMN eds_release_to SET STATISTICS 250;


--
-- Name: da_eds_saa_models; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_eds_saa_models (
    da_esm_saa_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_esm_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_esm_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_eds_saa_models ALTER COLUMN da_esm_saa_no SET STATISTICS 250;


--
-- Name: da_eds_saa_remarks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_eds_saa_remarks (
    desr_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    desr_rev_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    desr_remark_no character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    desr_remark character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    desr_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_eds_saa_ww_flags; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_eds_saa_ww_flags (
    desw_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    desw_rev_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    desw_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    desw_text character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_einpas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_einpas (
    ep_hg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_g character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_einpasdsc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_einpasdsc (
    ep_hg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_g character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_picture text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_einpashmmsm; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_einpashmmsm (
    ep_series character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_hgdest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_gdest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_tudest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_einpaskgtu; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_einpaskgtu (
    ep_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_hgdest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_gdest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_tudest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_modeltype character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_einpasops; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_einpasops (
    ep_group character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_scope character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_saaprefix character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_hgdest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_gdest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_tudest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_epc_fn_content; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_epc_fn_content (
    defc_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defc_text_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defc_line_no character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defc_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defc_abbr character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_epc_fn_content ALTER COLUMN defc_text_id SET STATISTICS 250;
ALTER TABLE ONLY public.da_epc_fn_content ALTER COLUMN defc_text SET STATISTICS 250;


--
-- Name: da_epc_fn_katalog_ref; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_epc_fn_katalog_ref (
    defr_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defr_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defr_fn_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defr_text_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defr_group character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_epc_fn_sa_ref; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_epc_fn_sa_ref (
    defs_sa_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defs_fn_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defs_text_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    defs_group character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_error_location; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_error_location (
    del_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    del_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    del_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    del_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    del_pose character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    del_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    del_damage_part character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    del_sda character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    del_sdb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    del_ord character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    del_userid character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_error_location ALTER COLUMN del_partno SET STATISTICS 250;
ALTER TABLE ONLY public.da_error_location ALTER COLUMN del_sda SET STATISTICS 250;


--
-- Name: da_es1; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_es1 (
    des_es1 character varying(2) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    des_fnid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    des_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_export_content; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_export_content (
    dec_job_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dec_do_type character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dec_do_id character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dec_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dec_state character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dec_error_text text COLLATE pg_catalog."en-US-x-icu",
    dec_archive_file character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dec_number_pictures character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dec_number_partlist_items character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dec_archive_size character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_export_request; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_export_request (
    der_job_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_customer_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_job_id_extern character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_languages text COLLATE pg_catalog."en-US-x-icu",
    der_include_sas character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_include_pictures character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_include_aggs character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_output_format character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_direct_download character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_creation_user_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_creation_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_completion_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_state character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_error_text text COLLATE pg_catalog."en-US-x-icu",
    der_save_location character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_collection_archive_file character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_picture_format character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_include_mat_properties character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    der_include_einpas character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_factories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_factories (
    df_letter_code character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    df_factory_no character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    df_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    df_pem_letter_code character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    df_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    df_filter_not_rel character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_factory_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_factory_data (
    dfd_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_factory character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_spkz character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_data_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_seq_no character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_product_grp character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_pose character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_posv character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_ww character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_et character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_pema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_pemb character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_pemta character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_pemtb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_crn text COLLATE pg_catalog."en-US-x-icu",
    dfd_stca character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_stcb character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_fn_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_event_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_event_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfd_linked character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_factory_data ALTER COLUMN dfd_guid SET STATISTICS 1000;
ALTER TABLE ONLY public.da_factory_data ALTER COLUMN dfd_adat SET STATISTICS 500;
ALTER TABLE ONLY public.da_factory_data ALTER COLUMN dfd_sdata SET STATISTICS 500;
ALTER TABLE ONLY public.da_factory_data ALTER COLUMN dfd_pema SET STATISTICS 250;
ALTER TABLE ONLY public.da_factory_data ALTER COLUMN dfd_pemb SET STATISTICS 250;


--
-- Name: da_factory_model; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_factory_model (
    dfm_wmi character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfm_factory_sign character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfm_factory character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfm_model_prefix character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfm_add_factory character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfm_agg_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfm_seq_no character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfm_belt_sign character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfm_belt_grouping character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfm_factory_sign_grouping character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_fn; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_fn (
    dfn_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfn_name character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfn_standard character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfn_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_fn ALTER COLUMN dfn_id SET STATISTICS 250;


--
-- Name: da_fn_content; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_fn_content (
    dfnc_fnid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnc_line_no character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnc_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnc_text_neutral character varying(1000) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_fn_content ALTER COLUMN dfnc_fnid SET STATISTICS 250;
ALTER TABLE ONLY public.da_fn_content ALTER COLUMN dfnc_text SET STATISTICS 250;


--
-- Name: da_fn_katalog_ref; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_fn_katalog_ref (
    dfnk_module character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnk_modver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnk_seqno character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnk_fnid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnk_fn_seqno character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnk_fn_marked character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnk_colortablefootnote character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_fn_katalog_ref ALTER COLUMN dfnk_fnid SET STATISTICS 250;


--
-- Name: da_fn_mat_ref; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_fn_mat_ref (
    dfnm_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnm_fnid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnm_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_fn_mat_ref ALTER COLUMN dfnm_matnr SET STATISTICS 250;


--
-- Name: da_fn_pos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_fn_pos (
    dfnp_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnp_sesi character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnp_posp character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnp_fn_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnp_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfnp_product_grp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_fn_saa_ref; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_fn_saa_ref (
    dfns_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfns_fnid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dfns_fn_seqno character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_generic_install_location; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_generic_install_location (
    dgil_series character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_pose character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_sda character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_sdb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_sesi character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_fed character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_hierarchy character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_pos_key character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_mk_sign character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_pet_sign character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_pwk_sign character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_ptk_sign character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_info_text character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_delete_sign character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_split_sign character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgil_gen_install_location character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_generic_install_location ALTER COLUMN dgil_sda SET STATISTICS 250;
ALTER TABLE ONLY public.da_generic_install_location ALTER COLUMN dgil_pos_key SET STATISTICS 250;


--
-- Name: da_generic_part; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_generic_part (
    dgp_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_pose character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_sesi character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_posp character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_posv character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_ww character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_etz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_generic_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_variantno character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_solution character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_generic_part ALTER COLUMN dgp_guid SET STATISTICS 250;


--
-- Name: da_genvo_pairing; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_genvo_pairing (
    dgp_genvo_l character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dgp_genvo_r character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_genvo_supp_text; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_genvo_supp_text (
    da_genvo_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_genvo_descr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_hmmsm; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_hmmsm (
    dh_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_hidden character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_no_calculation character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_special_calc_omitted_parts character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_hmmsm_kgtu; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_hmmsm_kgtu (
    dhk_bcte character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dhk_br_hmmsm character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dhk_kg_prediction character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dhk_tu_prediction character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_hmmsm_kgtu ALTER COLUMN dhk_bcte SET STATISTICS 1000;


--
-- Name: da_hmmsmdesc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_hmmsmdesc (
    dh_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_sm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_picture text COLLATE pg_catalog."en-US-x-icu",
    dh_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_factories character varying(40) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_ghm character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_ghs character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_kgu character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_pri character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dh_sales_kz character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_hmo_saa_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_hmo_saa_mapping (
    dhsm_hmo character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dhsm_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_include_const_mat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_include_const_mat (
    dicm_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dicm_sdata character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dicm_include_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dicm_include_part_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_include_part; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_include_part (
    dip_vari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dip_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dip_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dip_replace_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dip_replace_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dip_seqno character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dip_include_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dip_include_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_internal_text; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_internal_text (
    dit_u_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dit_creation_date character varying(25) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dit_do_type character varying(80) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dit_do_id character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dit_change_date character varying(25) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dit_titel character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dit_text text COLLATE pg_catalog."en-US-x-icu",
    dit_attachment bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_invoice_relevance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_invoice_relevance (
    dir_do_type character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dir_field character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_kem_masterdata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_kem_masterdata (
    dkm_kem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_sda character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_sdb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_output_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_handling_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_worker_idx character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_secrecy_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_secrecy_level character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_application_no character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_reason_code character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_spec character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_remark character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_permission_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_permission_data character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_technical_letter_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_special_tool_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_emission_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_stop_kem_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_stop_kem character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_annulment_kem character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_annulment_date character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_extension_date character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_joined_kem1 character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_joined_kem2 character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_joined_kem3 character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_joined_kem4 character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_sp_handling_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_sp_joined_kem character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_sp_data character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_sp_datr character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_sp_bt_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_sp_foreign_lang_proc character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_reason character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_kem_revision_state character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_tdat_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_system_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_skem character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_priority character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_deviation_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_deviation_planned_start character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_deviation_planned_end character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkm_deviation_duration character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_kem_masterdata ALTER COLUMN dkm_kem SET STATISTICS 500;
ALTER TABLE ONLY public.da_kem_masterdata ALTER COLUMN dkm_sda SET STATISTICS 500;
ALTER TABLE ONLY public.da_kem_masterdata ALTER COLUMN dkm_desc SET STATISTICS 500;
ALTER TABLE ONLY public.da_kem_masterdata ALTER COLUMN dkm_spec SET STATISTICS 500;
ALTER TABLE ONLY public.da_kem_masterdata ALTER COLUMN dkm_remark SET STATISTICS 250;
ALTER TABLE ONLY public.da_kem_masterdata ALTER COLUMN dkm_joined_kem1 SET STATISTICS 250;
ALTER TABLE ONLY public.da_kem_masterdata ALTER COLUMN dkm_reason SET STATISTICS 500;
ALTER TABLE ONLY public.da_kem_masterdata ALTER COLUMN dkm_skem SET STATISTICS 500;


--
-- Name: da_kem_response_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_kem_response_data (
    krd_factory character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    krd_kem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    krd_fin character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    krd_fin_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    krd_kem_unknown character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_kem_work_basket; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_kem_work_basket (
    dkwb_kem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwb_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwb_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwb_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwb_module_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwb_docu_relevant character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_kem_work_basket_mbs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_kem_work_basket_mbs (
    dkwm_kem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwm_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwm_group character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwm_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwm_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwm_module_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dkwm_docu_relevant character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_kgtu_as; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_kgtu_as (
    da_dkm_product character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkm_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkm_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkm_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkm_edat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkm_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkm_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_kgtu_template; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_kgtu_template (
    da_dkt_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkt_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkt_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkt_picture text COLLATE pg_catalog."en-US-x-icu",
    da_dkt_aggregate_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkt_as_product_class character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_dkt_tu_options text COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_message (
    dmsg_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmsg_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmsg_do_type character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmsg_do_id character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmsg_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmsg_subject character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmsg_message text COLLATE pg_catalog."en-US-x-icu",
    dmsg_creation_user_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmsg_creation_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmsg_resubmission_date character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_message_to; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_message_to (
    dmt_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmt_user_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmt_group_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmt_organisation_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmt_role_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmt_read_by_user_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmt_read_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_model; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_model (
    dm_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_name character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_code text COLLATE pg_catalog."en-US-x-icu",
    dm_horsepower character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_kilowatts character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_sales_title character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_development_title character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_drive_system character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_engine_concept character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_cylinder_count character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_engine_kind character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_steering character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_product_grp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_data character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_datb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_model_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_model_visible character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_as_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_as_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_model_invalid character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_comment character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_techdata character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_valid_from character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_valid_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_add_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_manual_change character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_const_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_filter_relevant character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_not_docu_relevant character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_model_suffix character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_model_building_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_model_building_code (
    dmbc_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmbc_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmbc_code character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_model_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_model_data (
    dmd_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_name character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_horsepower character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_kilowatts character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_development_title character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_model_invalid character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_drive_system character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_engine_concept character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_cylinder_count character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_engine_kind character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_data character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_datb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmd_sales_title character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_model_element_usage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_model_element_usage (
    dmeu_modelno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_module character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_sub_module character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_pos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_steering character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_legacy_number character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_revfrom character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_revto character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_kemfrom character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_kemto character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_release_from character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_release_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_sub_element character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_rfg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_quantity character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_pgkz character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmeu_code text COLLATE pg_catalog."en-US-x-icu",
    dmeu_plantsupply character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_model_element_usage ALTER COLUMN dmeu_kemfrom SET STATISTICS 250;
ALTER TABLE ONLY public.da_model_element_usage ALTER COLUMN dmeu_kemto SET STATISTICS 250;
ALTER TABLE ONLY public.da_model_element_usage ALTER COLUMN dmeu_release_from SET STATISTICS 250;
ALTER TABLE ONLY public.da_model_element_usage ALTER COLUMN dmeu_release_to SET STATISTICS 250;
ALTER TABLE ONLY public.da_model_element_usage ALTER COLUMN dmeu_sub_element SET STATISTICS 250;


--
-- Name: da_model_oil; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_model_oil (
    dmo_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmo_spec_validity character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmo_fluid_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmo_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmo_code_validity text COLLATE pg_catalog."en-US-x-icu",
    dmo_text_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmo_sae_class character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_model_oil_quantity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_model_oil_quantity (
    dmoq_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmoq_code_validity character varying(400) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmoq_fluid_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmoq_ident_to character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmoq_ident_from character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmoq_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_model_properties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_model_properties (
    dma_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_data character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_datb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_steering character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_product_grp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_code text COLLATE pg_catalog."en-US-x-icu",
    dma_as_relevant character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_models_aggs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_models_aggs (
    dma_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_aggregate_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dma_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_module; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_module (
    dm_module_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_docutype character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_spring_filter character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_variants_visible character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_use_color_tablefn character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_module_hidden character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_spec character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_pos_pic_check_inactive character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_source_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_hotspot_pic_check_inactive character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_zb_part_no_agg_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dm_special_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_module ALTER COLUMN dm_module_no SET STATISTICS 250;


--
-- Name: da_module_category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_module_category (
    dmc_module character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmc_as_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmc_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmc_picture text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_module_cemat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_module_cemat (
    dmc_module_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmc_lfdnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmc_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmc_einpas_hg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmc_einpas_g character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmc_einpas_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dmc_versions text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_module_cemat ALTER COLUMN dmc_partno SET STATISTICS 250;


--
-- Name: da_modules_einpas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_modules_einpas (
    dme_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_module_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_einpas_hg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_einpas_g character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_einpas_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_source_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_source_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_source_hm character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_source_m character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_source_sm character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_sort character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dme_saa_validity text COLLATE pg_catalog."en-US-x-icu",
    dme_code_validity text COLLATE pg_catalog."en-US-x-icu",
    dme_model_validity character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_modules_einpas ALTER COLUMN dme_module_no SET STATISTICS 250;


--
-- Name: da_nutzdok_annotation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_nutzdok_annotation (
    dna_ref_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dna_ref_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dna_ets character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dna_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dna_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dna_author character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dna_annotation character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_nutzdok_kem; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_nutzdok_kem (
    dnk_kem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_group character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_to_from_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_flash_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_evo_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_priority_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_tc_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_distribution character varying(120) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_evaluation_mark character varying(120) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_ets text COLLATE pg_catalog."en-US-x-icu",
    dnk_last_user character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_docu_start_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_docu_team character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_docu_user character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_remark character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_simplified_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_paper_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_processing_state character varying(30) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_processed_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_pem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_pem_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_pem_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_manual_start_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnk_ets_unconfirmed text COLLATE pg_catalog."en-US-x-icu",
    dnk_scope_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_nutzdok_remark; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_nutzdok_remark (
    dnr_ref_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnr_ref_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnr_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnr_last_user character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnr_last_modified character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dnr_remark bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_nutzdok_saa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_nutzdok_saa (
    dns_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_group character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_to_from_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_flash_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_evo_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_priority_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_tc_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_distribution character varying(120) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_evaluation_flag character varying(120) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_ets text COLLATE pg_catalog."en-US-x-icu",
    dns_last_user character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_docu_start_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_plan_number character varying(25) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_manual_start_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_begin_usage_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_processing_state character varying(30) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_processed_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dns_ets_unconfirmed text COLLATE pg_catalog."en-US-x-icu",
    dns_scope_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_omitted_parts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_omitted_parts (
    da_op_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_ops_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_ops_group (
    dog_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dog_group character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dog_as_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dog_as_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dog_invalid character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dog_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dog_picture text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_ops_scope; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_ops_scope (
    dos_scope character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dos_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dos_picture text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_partslist_mbs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_partslist_mbs (
    dpm_snr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_pos character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_sort character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_kem_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_kem_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_release_from character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_release_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_sub_snr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_sub_snr_suffix character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_quantity_flag character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_code text COLLATE pg_catalog."en-US-x-icu",
    dpm_snr_text character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_remark_id character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_remark_text character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_ww_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_ww_text character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_service_const_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_ctt_quantity_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_partslist_mbs ALTER COLUMN dpm_sort SET STATISTICS 250;
ALTER TABLE ONLY public.da_partslist_mbs ALTER COLUMN dpm_sub_snr SET STATISTICS 250;


--
-- Name: da_pem_masterdata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_pem_masterdata (
    dpm_pem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_factory_no character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_pem_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_desc character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_stc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_pem_masterdata ALTER COLUMN dpm_pem SET STATISTICS 250;
ALTER TABLE ONLY public.da_pem_masterdata ALTER COLUMN dpm_adat SET STATISTICS 250;


--
-- Name: da_pic_reference; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_pic_reference (
    dpr_ref_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpr_ref_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpr_mc_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpr_mc_rev_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpr_var_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpr_var_rev_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpr_error_code character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpr_error_text text COLLATE pg_catalog."en-US-x-icu",
    dpr_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpr_last_modified character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpr_previous_dates character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpr_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_pic_reference ALTER COLUMN dpr_ref_id SET STATISTICS 250;
ALTER TABLE ONLY public.da_pic_reference ALTER COLUMN dpr_guid SET STATISTICS 250;


--
-- Name: da_pic_to_attachment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_pic_to_attachment (
    da_pta_picorder character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_pta_attachment character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_picorder; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_picorder (
    po_order_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_order_id_extern character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_order_revision_extern character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_proposed_name character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_picture_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_user_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_user_group_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_last_error_code character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_last_error_text text COLLATE pg_catalog."en-US-x-icu",
    po_orderdate character varying(25) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_targetdate character varying(25) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_createdate character varying(25) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_description character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_job_user character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_job_group character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_job_role character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_eventname character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_has_attachments character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_change_reason character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_original_picorder character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_codes text COLLATE pg_catalog."en-US-x-icu",
    po_event_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_event_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_order_invalid character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_status_change_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_is_template character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_automation_level character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_is_copy character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_original_order_for_copy character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_only_fin_visible character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    po_invalid_image_data character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_picorder_attachments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_picorder_attachments (
    dpa_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpa_name character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpa_desc character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpa_size character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpa_size_base64 character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpa_content bytea,
    dpa_filetype character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpa_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpa_errortext text COLLATE pg_catalog."en-US-x-icu",
    dpa_errorcode character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_picorder_modules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_picorder_modules (
    pom_order_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pom_module_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_picorder_parts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_picorder_parts (
    ppa_order_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_vari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_ver character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_lfdnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_pos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_sach character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_src_key character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_zgs character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_reldate character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_context character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_sent character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_partlist_entry_data bytea,
    ppa_pic_position_marker character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ppa_seq_no character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_picorder_parts ALTER COLUMN ppa_sach SET STATISTICS 250;


--
-- Name: da_picorder_pictures; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_picorder_pictures (
    pop_order_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pop_pic_itemid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pop_pic_itemrevid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pop_used character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pop_designer character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pop_var_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pop_last_modified character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_picorder_usage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_picorder_usage (
    pou_order_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pou_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pou_einpas_hg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pou_einpas_g character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pou_einpas_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pou_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pou_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_ppua; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_ppua (
    da_ppua_partno character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_ppua_region character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_ppua_series character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_ppua_entity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_ppua_type character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_ppua_year character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_ppua_value character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_primus_include_part; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_primus_include_part (
    pip_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pip_include_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pip_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_primus_replace_part; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_primus_replace_part (
    prp_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    prp_successor_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    prp_brand character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    prp_pss_code_forward character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    prp_pss_code_back character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    prp_pss_info_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    prp_lifecycle_state character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_primus_replace_part ALTER COLUMN prp_part_no SET STATISTICS 500;
ALTER TABLE ONLY public.da_primus_replace_part ALTER COLUMN prp_successor_partno SET STATISTICS 250;


--
-- Name: da_product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_product (
    dp_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_structuring_type character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_title character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_picture text COLLATE pg_catalog."en-US-x-icu",
    dp_product_grp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_aggregate_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_assortment_classes text COLLATE pg_catalog."en-US-x-icu",
    dp_docu_method character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_product_visible character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_kz_delta character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_migration character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_migration_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_dataset_date character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_asproduct_classes text COLLATE pg_catalog."en-US-x-icu",
    dp_comment character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_series_ref character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_is_special_cat character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_aps_remark character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_aps_code character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_aps_from_idents character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_aps_to_idents character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_ident_class_old character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_epc_relevant character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_valid_countries text COLLATE pg_catalog."en-US-x-icu",
    dp_invalid_countries text COLLATE pg_catalog."en-US-x-icu",
    dp_brand text COLLATE pg_catalog."en-US-x-icu",
    dp_second_parts_enabled character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_ttz_filter character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_scoring_with_mcodes character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_disabled_filters text COLLATE pg_catalog."en-US-x-icu",
    dp_modification_timestamp character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_show_sas character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_cab_fallback character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_no_primus_hints character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_psk character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_use_svgs character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_prefer_svg character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_ident_factory_filtering character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_full_language_support character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_es_export_timestamp character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_dialog_pos_check character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_supplier_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_car_perspective character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_use_factory character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_connect_data_visible character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_fins character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_product_factories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_product_factories (
    dpf_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpf_factory_no character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpf_edat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpf_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_product_models; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_product_models (
    dpm_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_steering character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_textnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_valid_from character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_valid_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_model_visible character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_product_modules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_product_modules (
    dpm_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dpm_module_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_product_modules ALTER COLUMN dpm_module_no SET STATISTICS 250;


--
-- Name: da_product_sas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_product_sas (
    dps_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dps_sa_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dps_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dps_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_product_series; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_product_series (
    dps_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dps_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_pseudo_pem_date; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_pseudo_pem_date (
    dpd_pem_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_psk_product_variants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_psk_product_variants (
    dppv_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dppv_variant_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dppv_name1 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dppv_name2 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dppv_supply_number character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_replace_const_mat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_replace_const_mat (
    drcm_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcm_sdata character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcm_pre_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcm_vor_kz_k character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcm_rfme character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcm_pre_rfme character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcm_lock_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcm_anfo character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_replace_const_mat ALTER COLUMN drcm_part_no SET STATISTICS 250;
ALTER TABLE ONLY public.da_replace_const_mat ALTER COLUMN drcm_pre_part_no SET STATISTICS 250;


--
-- Name: da_replace_const_part; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_replace_const_part (
    drcp_part_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcp_sdata character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcp_sdatb character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcp_factory_ids character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcp_rfme character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcp_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcp_pre_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcp_replace_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcp_available_material character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcp_tool_change character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcp_material_change character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_replace_const_part ALTER COLUMN drcp_part_no SET STATISTICS 500;
ALTER TABLE ONLY public.da_replace_const_part ALTER COLUMN drcp_sdata SET STATISTICS 500;
ALTER TABLE ONLY public.da_replace_const_part ALTER COLUMN drcp_sdatb SET STATISTICS 250;
ALTER TABLE ONLY public.da_replace_const_part ALTER COLUMN drcp_text SET STATISTICS 500;
ALTER TABLE ONLY public.da_replace_const_part ALTER COLUMN drcp_pre_matnr SET STATISTICS 250;
ALTER TABLE ONLY public.da_replace_const_part ALTER COLUMN drcp_replace_matnr SET STATISTICS 250;


--
-- Name: da_replace_part; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_replace_part (
    drp_vari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_seqno character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_replace_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_replace_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_replace_rfmea character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_replace_rfmen character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_source_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_replace_source_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_replace_part ALTER COLUMN drp_vari SET STATISTICS 250;
ALTER TABLE ONLY public.da_replace_part ALTER COLUMN drp_replace_matnr SET STATISTICS 250;
ALTER TABLE ONLY public.da_replace_part ALTER COLUMN drp_source_guid SET STATISTICS 250;
ALTER TABLE ONLY public.da_replace_part ALTER COLUMN drp_replace_source_guid SET STATISTICS 250;


--
-- Name: da_report_const_nodes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_report_const_nodes (
    drcn_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcn_node_id character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcn_changeset_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcn_open_entries character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcn_changed_entries character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drcn_calculation_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_reserved_pk; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_reserved_pk (
    drp_do_type character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_do_id character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drp_change_set_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_response_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_response_data (
    drd_factory character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_bmaa character varying(40) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_pem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_ident character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_as_data character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_steering character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_agg_type character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_valid character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_whc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drd_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_response_data ALTER COLUMN drd_pem SET STATISTICS 250;
ALTER TABLE ONLY public.da_response_data ALTER COLUMN drd_adat SET STATISTICS 250;
ALTER TABLE ONLY public.da_response_data ALTER COLUMN drd_ident SET STATISTICS 250;
ALTER TABLE ONLY public.da_response_data ALTER COLUMN drd_text SET STATISTICS 250;


--
-- Name: da_response_spikes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_response_spikes (
    drs_factory character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_bmaa character varying(40) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_ident character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_spike_ident character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_pem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_as_data character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_steering character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_valid character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    drs_status character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_sa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_sa (
    ds_sa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_edat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_codes character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_not_docu_relevant character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_const_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_const_sa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_sa_modules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_sa_modules (
    dsm_sa_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_module_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_saa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_saa (
    ds_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_const_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_desc_extended character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_remark character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_rev_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_edat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_connected_sas character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_saa_ref character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_const_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_saa ALTER COLUMN ds_saa SET STATISTICS 250;
ALTER TABLE ONLY public.da_saa ALTER COLUMN ds_const_desc SET STATISTICS 250;


--
-- Name: da_saa_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_saa_history (
    dsh_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsh_rev_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsh_rev_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsh_kem_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsh_kem_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsh_release_from character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsh_release_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsh_factory_ids character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_saa_history ALTER COLUMN dsh_saa SET STATISTICS 250;
ALTER TABLE ONLY public.da_saa_history ALTER COLUMN dsh_kem_from SET STATISTICS 250;
ALTER TABLE ONLY public.da_saa_history ALTER COLUMN dsh_kem_to SET STATISTICS 250;
ALTER TABLE ONLY public.da_saa_history ALTER COLUMN dsh_release_from SET STATISTICS 250;
ALTER TABLE ONLY public.da_saa_history ALTER COLUMN dsh_release_to SET STATISTICS 250;


--
-- Name: da_scope_kg_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_scope_kg_mapping (
    dskm_scope_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dskm_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_series; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_series (
    ds_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_name character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_data character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_datb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_product_grp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_component_flag character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_spare_part character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_import_relevant character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_event_flag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_alternative_calc character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_hierarchy character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_merge_products character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_auto_calculation character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_aa_wo_factory_data text COLLATE pg_catalog."en-US-x-icu",
    ds_v_position_check character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_series_aggs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_series_aggs (
    dsa_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsa_aggseries_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_series_codes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_series_codes (
    dsc_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_group character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_pos character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_posv character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_sdata character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_sdatb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_regulation character varying(30) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_steering character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_cgkz character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_zbed character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_rfg character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_distr character varying(150) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_fed character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_product_grp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_codes text COLLATE pg_catalog."en-US-x-icu",
    dsc_feasibility_cond character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_global_code_sign character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_event_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsc_event_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_series_codes ALTER COLUMN dsc_sdata SET STATISTICS 250;
ALTER TABLE ONLY public.da_series_codes ALTER COLUMN dsc_sdatb SET STATISTICS 250;


--
-- Name: da_series_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_series_events (
    dse_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dse_event_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dse_sdata character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dse_sdatb character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dse_previous_event_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dse_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dse_remark character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dse_conv_relevant character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dse_status character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dse_codes text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_series_expdate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_series_expdate (
    dsed_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsed_aa character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsed_factory_no character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsed_exp_date character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_series_sop; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_series_sop (
    dsp_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsp_aa character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsp_start_of_prod character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsp_kem_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsp_active character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_spk_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_spk_mapping (
    spkm_series_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    spkm_hm character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    spkm_m character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    spkm_kurz_e character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    spkm_kurz_as character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    spkm_lang_e character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    spkm_lang_as character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    spkm_connector_e character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    spkm_connector_as character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    spkm_steering character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_spring_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_spring_mapping (
    dsm_zb_spring_leg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_spring character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_edat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_adat character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_structure; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_structure (
    ds_parent character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_child character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_title character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_sort character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_construction character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_picture text COLLATE pg_catalog."en-US-x-icu",
    ds_model_type_prefix character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ds_asproduct_classes text COLLATE pg_catalog."en-US-x-icu",
    ds_aggregate_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_structure_mbs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_structure_mbs (
    dsm_snr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_snr_suffix character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_pos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_sort character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_kem_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_kem_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_release_from character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_release_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_sub_snr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_sub_snr_suffix character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_code text COLLATE pg_catalog."en-US-x-icu",
    dsm_snr_text character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsm_ctt_quantity_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_sub_module_category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_sub_module_category (
    dsmc_sub_module character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsmc_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dsmc_picture text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_supplier_partno_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_supplier_partno_mapping (
    dspm_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dspm_supplier_partno character varying(80) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dspm_supplier_no character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dspm_supplier_name character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dspm_supplier_partno_plain character varying(80) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_supplier_partno_mapping ALTER COLUMN dspm_partno SET STATISTICS 250;
ALTER TABLE ONLY public.da_supplier_partno_mapping ALTER COLUMN dspm_supplier_partno SET STATISTICS 250;
ALTER TABLE ONLY public.da_supplier_partno_mapping ALTER COLUMN dspm_supplier_partno_plain SET STATISTICS 250;


--
-- Name: da_top_tus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_top_tus (
    dtt_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtt_country_code character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtt_kg character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtt_tu character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dtt_rank character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_transit_lang_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_transit_lang_mapping (
    da_tlm_transit_language character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_tlm_iso_language character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_tlm_comment character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_tlm_lang_id character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_um_groups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_um_groups (
    da_g_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_g_id character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_g_alias character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_g_supplier_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_g_branch character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_um_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_um_roles (
    da_r_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_r_id character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_r_alias character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_um_user_groups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_um_user_groups (
    da_ug_uguid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_ug_gguid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_um_user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_um_user_roles (
    da_ur_uguid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_ur_rguid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_um_users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_um_users (
    da_u_guid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_u_id character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_u_alias character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_u_title character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_u_firstname character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    da_u_lastname character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_vehicle_datacard_codes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_vehicle_datacard_codes (
    dvdc_code character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_vin_model_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_vin_model_mapping (
    dvm_vin_prefix character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dvm_model_prefix character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_vs2us_relation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_vs2us_relation (
    vur_vehicle_series character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_vs_pos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_vs_posv character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_unit_series character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_data character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_datb character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_group character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_steering character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_rfg character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_quantity character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_distr character varying(150) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_fed character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_product_grp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_codes text COLLATE pg_catalog."en-US-x-icu",
    vur_event_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    vur_event_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_wb_saa_calculation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_wb_saa_calculation (
    wsc_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    wsc_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    wsc_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    wsc_min_release_from character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    wsc_max_release_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    wsc_code text COLLATE pg_catalog."en-US-x-icu",
    wsc_factories character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.da_wb_saa_calculation ALTER COLUMN wsc_saa SET STATISTICS 250;
ALTER TABLE ONLY public.da_wb_saa_calculation ALTER COLUMN wsc_min_release_from SET STATISTICS 250;


--
-- Name: da_wb_saa_states; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_wb_saa_states (
    wbs_model_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    wbs_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    wbs_saa character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    wbs_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    wbs_docu_relevant character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_wb_supplier_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_wb_supplier_mapping (
    dwsm_model_type_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwsm_product_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwsm_kg_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwsm_kg_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwsm_supplier_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_wh_simplified_parts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_wh_simplified_parts (
    dwhs_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwhs_successor_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_wire_harness; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_wire_harness (
    dwh_snr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwh_ref character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwh_connector_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwh_sub_snr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwh_pos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwh_snr_type character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwh_contact_dataset_date character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwh_part_dataset_date character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwh_contact_add_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_workorder; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_workorder (
    dwo_bst_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_order_no character varying(25) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_series character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_branch character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_sub_branches character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_cost_neutral character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_internal_order character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_release_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_title character varying(1000) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_delivery_date_planned character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_start_of_work character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_supplier_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_supplier_shortname character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwo_supplier_name character varying(255) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: da_workorder_tasks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.da_workorder_tasks (
    dwt_bst_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwt_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwt_activity_name character varying(255) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwt_activity_type character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwt_amount character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: doku; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.doku (
    d_sprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_nr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_titel character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_file character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_nofultxt character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_md5 character varying(32) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: dokulink; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dokulink (
    d_kvari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_kver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_mver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_sprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_knoten character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_knver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_text character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_nr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_dsprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_seite character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_vknoten character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_vknver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_vsprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_kap character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_extview character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    d_seqnr character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: dokurefs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dokurefs (
    dr_dokumd5 character varying(32) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dr_file character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dr_reffilename character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: dtag_da_code_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_code_view AS
 SELECT da_code.dc_code_id,
    da_code.dc_series_no,
    da_code.dc_pgrp,
    da_code.dc_sdata,
    da_code.dc_source,
    da_code.dc_sdatb,
    da_code.dc_desc,
    da_code.t_stamp
   FROM public.da_code
  WHERE (((da_code.dc_pgrp)::text = 'N'::text) AND ((da_code.dc_pgrp)::text = 'U'::text));


--
-- Name: dtag_da_color_number_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_color_number_view AS
 SELECT da_color_number.dcn_color_no,
    da_color_number.dcn_sda,
    da_color_number.dcn_desc,
    da_color_number.dcn_edat,
    da_color_number.dcn_adat,
    da_color_number.dcn_source,
    da_color_number.t_stamp
   FROM public.da_color_number;


--
-- Name: dtag_da_product_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_product_view AS
 SELECT da_product.dp_product_no,
    da_product.dp_structuring_type,
    da_product.dp_title,
    da_product.dp_picture,
    da_product.dp_product_grp,
    da_product.dp_aggregate_type,
    da_product.dp_assortment_classes,
    da_product.dp_docu_method,
    da_product.dp_product_visible,
    da_product.dp_kz_delta,
    da_product.dp_migration,
    da_product.dp_migration_date,
    da_product.dp_dataset_date,
    da_product.dp_source,
    da_product.dp_asproduct_classes,
    da_product.dp_comment,
    da_product.dp_series_ref,
    da_product.dp_is_special_cat,
    da_product.dp_aps_remark,
    da_product.dp_aps_code,
    da_product.dp_aps_from_idents,
    da_product.dp_aps_to_idents,
    da_product.dp_ident_class_old,
    da_product.dp_epc_relevant,
    da_product.dp_valid_countries,
    da_product.dp_invalid_countries,
    da_product.dp_brand,
    da_product.dp_second_parts_enabled,
    da_product.dp_ttz_filter,
    da_product.dp_scoring_with_mcodes,
    da_product.dp_disabled_filters,
    da_product.dp_modification_timestamp,
    da_product.dp_show_sas,
    da_product.dp_cab_fallback,
    da_product.t_stamp,
    da_product.dp_no_primus_hints,
    da_product.dp_psk,
    da_product.dp_use_svgs,
    da_product.dp_prefer_svg,
    da_product.dp_ident_factory_filtering,
    da_product.dp_full_language_support,
    da_product.dp_es_export_timestamp,
    da_product.dp_dialog_pos_check,
    da_product.dp_supplier_no,
    da_product.dp_car_perspective,
    da_product.dp_use_factory,
    da_product.dp_connect_data_visible,
    da_product.dp_fins
   FROM public.da_product
  WHERE ((da_product.dp_asproduct_classes ~~ '%<SOE>I</SOE>%'::text) OR (da_product.dp_asproduct_classes ~~ '%<SOE>K</SOE>%'::text) OR (da_product.dp_asproduct_classes ~~ '%<SOE>L</SOE>%'::text) OR (da_product.dp_asproduct_classes ~~ '%<SOE>O</SOE>%'::text) OR (da_product.dp_asproduct_classes ~~ '%<SOE>U</SOE>%'::text));


--
-- Name: dtag_da_module_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_module_view AS
 SELECT da_module.dm_module_no,
    da_module.t_stamp,
    da_module.dm_docutype,
    da_module.dm_spring_filter,
    da_module.dm_variants_visible,
    da_module.dm_use_color_tablefn,
    da_module.dm_module_hidden,
    da_module.dm_spec,
    da_module.dm_pos_pic_check_inactive,
    da_module.dm_source_tu,
    da_module.dm_hotspot_pic_check_inactive,
    da_module.dm_zb_part_no_agg_type,
    da_module.dm_special_tu
   FROM public.da_module
  WHERE ((da_module.dm_module_no)::text IN ( SELECT da_product_modules.dpm_module_no
           FROM public.da_product_modules
          WHERE ((da_product_modules.dpm_product_no)::text IN ( SELECT dtag_da_product_view.dp_product_no
                   FROM public.dtag_da_product_view))
        UNION
         SELECT ('SA-'::text || (da_product_sas.dps_sa_no)::text)
           FROM public.da_product_sas
          WHERE ((da_product_sas.dps_product_no)::text IN ( SELECT dtag_da_product_view.dp_product_no
                   FROM public.dtag_da_product_view))));


--
-- Name: dtag_da_comb_text_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_comb_text_view AS
 SELECT da_comb_text.dct_module,
    da_comb_text.dct_modver,
    da_comb_text.dct_seqno,
    da_comb_text.dct_text_seqno,
    da_comb_text.dct_dict_text,
    da_comb_text.dct_text_neutral,
    da_comb_text.t_stamp,
    da_comb_text.dct_source_genvo
   FROM public.da_comb_text
  WHERE ((da_comb_text.dct_module)::text IN ( SELECT dtag_da_module_view.dm_module_no
           FROM public.dtag_da_module_view));


--
-- Name: dtag_da_dict_meta_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_dict_meta_view AS
 SELECT da_dict_meta.da_dict_meta_txtkind_id,
    da_dict_meta.da_dict_meta_textid,
    da_dict_meta.da_dict_meta_foreignid,
    da_dict_meta.da_dict_meta_source,
    da_dict_meta.da_dict_meta_state,
    da_dict_meta.da_dict_meta_create,
    da_dict_meta.da_dict_meta_change,
    da_dict_meta.da_dict_meta_userid,
    da_dict_meta.da_dict_meta_dialogid,
    da_dict_meta.da_dict_meta_eldasid,
    da_dict_meta.t_stamp
   FROM public.da_dict_meta;


--
-- Name: dtag_da_factory_data_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_factory_data_view AS
 SELECT da_factory_data.dfd_guid,
    da_factory_data.dfd_factory,
    da_factory_data.dfd_spkz,
    da_factory_data.dfd_adat,
    da_factory_data.dfd_data_id,
    da_factory_data.dfd_seq_no,
    da_factory_data.dfd_product_grp,
    da_factory_data.dfd_series_no,
    da_factory_data.dfd_hm,
    da_factory_data.dfd_m,
    da_factory_data.dfd_sm,
    da_factory_data.dfd_pose,
    da_factory_data.dfd_posv,
    da_factory_data.dfd_ww,
    da_factory_data.dfd_et,
    da_factory_data.dfd_aa,
    da_factory_data.dfd_sdata,
    da_factory_data.dfd_pema,
    da_factory_data.dfd_pemb,
    da_factory_data.dfd_pemta,
    da_factory_data.dfd_pemtb,
    da_factory_data.dfd_crn,
    da_factory_data.dfd_stca,
    da_factory_data.dfd_stcb,
    da_factory_data.dfd_source,
    da_factory_data.dfd_fn_id,
    da_factory_data.dfd_status,
    da_factory_data.dfd_event_from,
    da_factory_data.dfd_event_to,
    da_factory_data.t_stamp,
    da_factory_data.dfd_linked
   FROM public.da_factory_data
  WHERE (((da_factory_data.dfd_series_no)::text = ''::text) OR (substr((da_factory_data.dfd_guid)::text, 1, (length((da_factory_data.dfd_guid)::text) - 6)) IN ( SELECT dtag_da_module_view.dm_module_no
           FROM public.dtag_da_module_view)));


--
-- Name: dtag_da_fn_content_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_fn_content_view AS
 SELECT da_fn_content.dfnc_fnid,
    da_fn_content.dfnc_line_no,
    da_fn_content.dfnc_text,
    da_fn_content.dfnc_text_neutral,
    da_fn_content.t_stamp
   FROM public.da_fn_content;


--
-- Name: dtag_da_fn_kat_ref_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_fn_kat_ref_view AS
 SELECT da_fn_katalog_ref.dfnk_module,
    da_fn_katalog_ref.dfnk_modver,
    da_fn_katalog_ref.dfnk_seqno,
    da_fn_katalog_ref.dfnk_fnid,
    da_fn_katalog_ref.dfnk_fn_seqno,
    da_fn_katalog_ref.dfnk_fn_marked,
    da_fn_katalog_ref.dfnk_colortablefootnote,
    da_fn_katalog_ref.t_stamp
   FROM public.da_fn_katalog_ref
  WHERE ((da_fn_katalog_ref.dfnk_module)::text IN ( SELECT dtag_da_module_view.dm_module_no
           FROM public.dtag_da_module_view));


--
-- Name: dtag_da_fn_mat_ref_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_fn_mat_ref_view AS
 SELECT da_fn_mat_ref.dfnm_matnr,
    da_fn_mat_ref.dfnm_fnid,
    da_fn_mat_ref.t_stamp,
    da_fn_mat_ref.dfnm_source
   FROM public.da_fn_mat_ref
  WHERE ((da_fn_mat_ref.dfnm_source)::text <> 'DIALOG'::text);


--
-- Name: dtag_da_fn_saa_ref_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_fn_saa_ref_view AS
 SELECT da_fn_saa_ref.dfns_saa,
    da_fn_saa_ref.dfns_fnid,
    da_fn_saa_ref.dfns_fn_seqno,
    da_fn_saa_ref.t_stamp
   FROM public.da_fn_saa_ref
  WHERE (substr((da_fn_saa_ref.dfns_saa)::text, 1, 7) IN ( SELECT da_product_sas.dps_sa_no
           FROM public.da_product_sas
          WHERE ((da_product_sas.dps_product_no)::text IN ( SELECT dtag_da_product_view.dp_product_no
                   FROM public.dtag_da_product_view))));


--
-- Name: dtag_da_fn_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_fn_view AS
 SELECT da_fn.dfn_id,
    da_fn.dfn_name,
    da_fn.dfn_standard,
    da_fn.t_stamp,
    da_fn.dfn_type
   FROM public.da_fn;


--
-- Name: dtag_da_include_part_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_include_part_view AS
 SELECT da_include_part.dip_vari,
    da_include_part.dip_ver,
    da_include_part.dip_lfdnr,
    da_include_part.dip_replace_matnr,
    da_include_part.dip_replace_lfdnr,
    da_include_part.dip_seqno,
    da_include_part.dip_include_matnr,
    da_include_part.dip_include_quantity,
    da_include_part.t_stamp
   FROM public.da_include_part
  WHERE ((da_include_part.dip_vari)::text IN ( SELECT dtag_da_module_view.dm_module_no
           FROM public.dtag_da_module_view));


--
-- Name: dtag_da_kgtu_as_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_kgtu_as_view AS
 SELECT da_kgtu_as.da_dkm_product,
    da_kgtu_as.da_dkm_kg,
    da_kgtu_as.da_dkm_tu,
    da_kgtu_as.da_dkm_desc,
    da_kgtu_as.da_dkm_edat,
    da_kgtu_as.da_dkm_adat,
    da_kgtu_as.da_dkm_source,
    da_kgtu_as.t_stamp
   FROM public.da_kgtu_as
  WHERE ((da_kgtu_as.da_dkm_product)::text IN ( SELECT dtag_da_product_view.dp_product_no
           FROM public.dtag_da_product_view));


--
-- Name: dtag_da_model_aggs_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_model_aggs_view AS
 SELECT da_models_aggs.dma_model_no,
    da_models_aggs.dma_aggregate_no,
    da_models_aggs.dma_source,
    da_models_aggs.t_stamp
   FROM public.da_models_aggs
  WHERE ((da_models_aggs.dma_model_no)::text IN ( SELECT da_product_models.dpm_model_no
           FROM (public.da_product_models
             JOIN ( SELECT dtag_da_product_view.dp_product_no,
                    dtag_da_product_view.dp_structuring_type,
                    dtag_da_product_view.dp_title,
                    dtag_da_product_view.dp_picture,
                    dtag_da_product_view.dp_product_grp,
                    dtag_da_product_view.dp_aggregate_type,
                    dtag_da_product_view.dp_assortment_classes,
                    dtag_da_product_view.dp_docu_method,
                    dtag_da_product_view.dp_product_visible,
                    dtag_da_product_view.dp_kz_delta,
                    dtag_da_product_view.dp_migration,
                    dtag_da_product_view.dp_migration_date,
                    dtag_da_product_view.dp_dataset_date,
                    dtag_da_product_view.dp_source,
                    dtag_da_product_view.dp_asproduct_classes,
                    dtag_da_product_view.dp_comment,
                    dtag_da_product_view.dp_series_ref,
                    dtag_da_product_view.dp_is_special_cat,
                    dtag_da_product_view.dp_aps_remark,
                    dtag_da_product_view.dp_aps_code,
                    dtag_da_product_view.dp_aps_from_idents,
                    dtag_da_product_view.dp_aps_to_idents,
                    dtag_da_product_view.dp_ident_class_old,
                    dtag_da_product_view.dp_epc_relevant,
                    dtag_da_product_view.dp_valid_countries,
                    dtag_da_product_view.dp_invalid_countries,
                    dtag_da_product_view.dp_brand,
                    dtag_da_product_view.dp_second_parts_enabled,
                    dtag_da_product_view.dp_ttz_filter,
                    dtag_da_product_view.dp_scoring_with_mcodes,
                    dtag_da_product_view.dp_disabled_filters,
                    dtag_da_product_view.dp_modification_timestamp,
                    dtag_da_product_view.dp_show_sas,
                    dtag_da_product_view.dp_cab_fallback,
                    dtag_da_product_view.t_stamp,
                    dtag_da_product_view.dp_no_primus_hints,
                    dtag_da_product_view.dp_psk,
                    dtag_da_product_view.dp_use_svgs,
                    dtag_da_product_view.dp_prefer_svg,
                    dtag_da_product_view.dp_ident_factory_filtering,
                    dtag_da_product_view.dp_full_language_support,
                    dtag_da_product_view.dp_es_export_timestamp,
                    dtag_da_product_view.dp_dialog_pos_check,
                    dtag_da_product_view.dp_supplier_no,
                    dtag_da_product_view.dp_car_perspective,
                    dtag_da_product_view.dp_use_factory,
                    dtag_da_product_view.dp_connect_data_visible,
                    dtag_da_product_view.dp_fins
                   FROM public.dtag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_product_models.dpm_product_no)::text)))));


--
-- Name: dtag_da_model_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_model_view AS
 SELECT da_model.dm_model_no,
    da_model.dm_series_no,
    da_model.dm_name,
    da_model.dm_code,
    da_model.dm_horsepower,
    da_model.dm_kilowatts,
    da_model.dm_sales_title,
    da_model.dm_development_title,
    da_model.dm_drive_system,
    da_model.dm_engine_concept,
    da_model.dm_cylinder_count,
    da_model.dm_engine_kind,
    da_model.dm_aa,
    da_model.dm_steering,
    da_model.dm_product_grp,
    da_model.dm_data,
    da_model.dm_datb,
    da_model.dm_model_type,
    da_model.dm_source,
    da_model.dm_model_visible,
    da_model.dm_as_from,
    da_model.dm_as_to,
    da_model.dm_model_invalid,
    da_model.dm_comment,
    da_model.dm_techdata,
    da_model.dm_valid_from,
    da_model.dm_valid_to,
    da_model.dm_add_text,
    da_model.dm_manual_change,
    da_model.dm_const_model_no,
    da_model.t_stamp,
    da_model.dm_filter_relevant,
    da_model.dm_not_docu_relevant,
    da_model.dm_model_suffix
   FROM public.da_model
  WHERE ((da_model.dm_model_no)::text IN ( SELECT da_product_models.dpm_model_no
           FROM (public.da_product_models
             JOIN public.dtag_da_product_view ON (((dtag_da_product_view.dp_product_no)::text = (da_product_models.dpm_product_no)::text)))));


--
-- Name: dtag_da_module_cemat_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_module_cemat_view AS
 SELECT da_module_cemat.dmc_module_no,
    da_module_cemat.dmc_lfdnr,
    da_module_cemat.dmc_partno,
    da_module_cemat.dmc_einpas_hg,
    da_module_cemat.dmc_einpas_g,
    da_module_cemat.dmc_einpas_tu,
    da_module_cemat.dmc_versions,
    da_module_cemat.t_stamp
   FROM public.da_module_cemat
  WHERE ((da_module_cemat.dmc_module_no)::text IN ( SELECT dtag_da_module_view.dm_module_no
           FROM public.dtag_da_module_view));


--
-- Name: dtag_da_modules_einpas_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_modules_einpas_view AS
 SELECT da_modules_einpas.dme_product_no,
    da_modules_einpas.dme_module_no,
    da_modules_einpas.dme_lfdnr,
    da_modules_einpas.dme_einpas_hg,
    da_modules_einpas.dme_einpas_g,
    da_modules_einpas.dme_einpas_tu,
    da_modules_einpas.dme_source_kg,
    da_modules_einpas.dme_source_tu,
    da_modules_einpas.dme_source_hm,
    da_modules_einpas.dme_source_m,
    da_modules_einpas.dme_source_sm,
    da_modules_einpas.dme_sort,
    da_modules_einpas.t_stamp,
    da_modules_einpas.dme_saa_validity,
    da_modules_einpas.dme_code_validity,
    da_modules_einpas.dme_model_validity
   FROM (public.da_modules_einpas
     JOIN ( SELECT dtag_da_product_view.dp_product_no,
            dtag_da_product_view.dp_structuring_type,
            dtag_da_product_view.dp_title,
            dtag_da_product_view.dp_picture,
            dtag_da_product_view.dp_product_grp,
            dtag_da_product_view.dp_aggregate_type,
            dtag_da_product_view.dp_assortment_classes,
            dtag_da_product_view.dp_docu_method,
            dtag_da_product_view.dp_product_visible,
            dtag_da_product_view.dp_kz_delta,
            dtag_da_product_view.dp_migration,
            dtag_da_product_view.dp_migration_date,
            dtag_da_product_view.dp_dataset_date,
            dtag_da_product_view.dp_source,
            dtag_da_product_view.dp_asproduct_classes,
            dtag_da_product_view.dp_comment,
            dtag_da_product_view.dp_series_ref,
            dtag_da_product_view.dp_is_special_cat,
            dtag_da_product_view.dp_aps_remark,
            dtag_da_product_view.dp_aps_code,
            dtag_da_product_view.dp_aps_from_idents,
            dtag_da_product_view.dp_aps_to_idents,
            dtag_da_product_view.dp_ident_class_old,
            dtag_da_product_view.dp_epc_relevant,
            dtag_da_product_view.dp_valid_countries,
            dtag_da_product_view.dp_invalid_countries,
            dtag_da_product_view.dp_brand,
            dtag_da_product_view.dp_second_parts_enabled,
            dtag_da_product_view.dp_ttz_filter,
            dtag_da_product_view.dp_scoring_with_mcodes,
            dtag_da_product_view.dp_disabled_filters,
            dtag_da_product_view.dp_modification_timestamp,
            dtag_da_product_view.dp_show_sas,
            dtag_da_product_view.dp_cab_fallback,
            dtag_da_product_view.t_stamp,
            dtag_da_product_view.dp_no_primus_hints,
            dtag_da_product_view.dp_psk,
            dtag_da_product_view.dp_use_svgs,
            dtag_da_product_view.dp_prefer_svg,
            dtag_da_product_view.dp_ident_factory_filtering,
            dtag_da_product_view.dp_full_language_support,
            dtag_da_product_view.dp_es_export_timestamp,
            dtag_da_product_view.dp_dialog_pos_check,
            dtag_da_product_view.dp_supplier_no,
            dtag_da_product_view.dp_car_perspective,
            dtag_da_product_view.dp_use_factory,
            dtag_da_product_view.dp_connect_data_visible,
            dtag_da_product_view.dp_fins
           FROM public.dtag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_modules_einpas.dme_product_no)::text)));


--
-- Name: dtag_da_product_factories_plant_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_product_factories_plant_view AS
 SELECT da_product_factories.dpf_product_no,
    da_product_factories.dpf_factory_no,
    da_product_factories.dpf_edat,
    da_product_factories.dpf_adat,
    da_product_factories.t_stamp
   FROM (public.da_product_factories
     JOIN ( SELECT dtag_da_product_view.dp_product_no,
            dtag_da_product_view.dp_structuring_type,
            dtag_da_product_view.dp_title,
            dtag_da_product_view.dp_picture,
            dtag_da_product_view.dp_product_grp,
            dtag_da_product_view.dp_aggregate_type,
            dtag_da_product_view.dp_assortment_classes,
            dtag_da_product_view.dp_docu_method,
            dtag_da_product_view.dp_product_visible,
            dtag_da_product_view.dp_kz_delta,
            dtag_da_product_view.dp_migration,
            dtag_da_product_view.dp_migration_date,
            dtag_da_product_view.dp_dataset_date,
            dtag_da_product_view.dp_source,
            dtag_da_product_view.dp_asproduct_classes,
            dtag_da_product_view.dp_comment,
            dtag_da_product_view.dp_series_ref,
            dtag_da_product_view.dp_is_special_cat,
            dtag_da_product_view.dp_aps_remark,
            dtag_da_product_view.dp_aps_code,
            dtag_da_product_view.dp_aps_from_idents,
            dtag_da_product_view.dp_aps_to_idents,
            dtag_da_product_view.dp_ident_class_old,
            dtag_da_product_view.dp_epc_relevant,
            dtag_da_product_view.dp_valid_countries,
            dtag_da_product_view.dp_invalid_countries,
            dtag_da_product_view.dp_brand,
            dtag_da_product_view.dp_second_parts_enabled,
            dtag_da_product_view.dp_ttz_filter,
            dtag_da_product_view.dp_scoring_with_mcodes,
            dtag_da_product_view.dp_disabled_filters,
            dtag_da_product_view.dp_modification_timestamp,
            dtag_da_product_view.dp_show_sas,
            dtag_da_product_view.dp_cab_fallback,
            dtag_da_product_view.t_stamp,
            dtag_da_product_view.dp_no_primus_hints,
            dtag_da_product_view.dp_psk,
            dtag_da_product_view.dp_use_svgs,
            dtag_da_product_view.dp_prefer_svg,
            dtag_da_product_view.dp_ident_factory_filtering,
            dtag_da_product_view.dp_full_language_support,
            dtag_da_product_view.dp_es_export_timestamp,
            dtag_da_product_view.dp_dialog_pos_check,
            dtag_da_product_view.dp_supplier_no,
            dtag_da_product_view.dp_car_perspective,
            dtag_da_product_view.dp_use_factory,
            dtag_da_product_view.dp_connect_data_visible,
            dtag_da_product_view.dp_fins
           FROM public.dtag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_product_factories.dpf_product_no)::text)));


--
-- Name: dtag_da_product_factories_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_product_factories_view AS
 SELECT da_product_factories.dpf_product_no,
    da_product_factories.dpf_factory_no,
    da_product_factories.dpf_edat,
    da_product_factories.dpf_adat,
    da_product_factories.t_stamp
   FROM (public.da_product_factories
     JOIN ( SELECT dtag_da_product_view.dp_product_no,
            dtag_da_product_view.dp_structuring_type,
            dtag_da_product_view.dp_title,
            dtag_da_product_view.dp_picture,
            dtag_da_product_view.dp_product_grp,
            dtag_da_product_view.dp_aggregate_type,
            dtag_da_product_view.dp_assortment_classes,
            dtag_da_product_view.dp_docu_method,
            dtag_da_product_view.dp_product_visible,
            dtag_da_product_view.dp_kz_delta,
            dtag_da_product_view.dp_migration,
            dtag_da_product_view.dp_migration_date,
            dtag_da_product_view.dp_dataset_date,
            dtag_da_product_view.dp_source,
            dtag_da_product_view.dp_asproduct_classes,
            dtag_da_product_view.dp_comment,
            dtag_da_product_view.dp_series_ref,
            dtag_da_product_view.dp_is_special_cat,
            dtag_da_product_view.dp_aps_remark,
            dtag_da_product_view.dp_aps_code,
            dtag_da_product_view.dp_aps_from_idents,
            dtag_da_product_view.dp_aps_to_idents,
            dtag_da_product_view.dp_ident_class_old,
            dtag_da_product_view.dp_epc_relevant,
            dtag_da_product_view.dp_valid_countries,
            dtag_da_product_view.dp_invalid_countries,
            dtag_da_product_view.dp_brand,
            dtag_da_product_view.dp_second_parts_enabled,
            dtag_da_product_view.dp_ttz_filter,
            dtag_da_product_view.dp_scoring_with_mcodes,
            dtag_da_product_view.dp_disabled_filters,
            dtag_da_product_view.dp_modification_timestamp,
            dtag_da_product_view.dp_show_sas,
            dtag_da_product_view.dp_cab_fallback,
            dtag_da_product_view.t_stamp,
            dtag_da_product_view.dp_no_primus_hints,
            dtag_da_product_view.dp_psk,
            dtag_da_product_view.dp_use_svgs,
            dtag_da_product_view.dp_prefer_svg,
            dtag_da_product_view.dp_ident_factory_filtering,
            dtag_da_product_view.dp_full_language_support,
            dtag_da_product_view.dp_es_export_timestamp,
            dtag_da_product_view.dp_dialog_pos_check,
            dtag_da_product_view.dp_supplier_no,
            dtag_da_product_view.dp_car_perspective,
            dtag_da_product_view.dp_use_factory,
            dtag_da_product_view.dp_connect_data_visible,
            dtag_da_product_view.dp_fins
           FROM public.dtag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_product_factories.dpf_product_no)::text)));


--
-- Name: dtag_da_product_models_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_product_models_view AS
 SELECT da_product_models.dpm_product_no,
    da_product_models.dpm_model_no,
    da_product_models.dpm_steering,
    da_product_models.dpm_textnr,
    da_product_models.dpm_valid_from,
    da_product_models.dpm_valid_to,
    da_product_models.dpm_model_visible,
    da_product_models.t_stamp
   FROM (public.da_product_models
     JOIN public.dtag_da_product_view ON (((dtag_da_product_view.dp_product_no)::text = (da_product_models.dpm_product_no)::text)));


--
-- Name: dtag_da_product_modules_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_product_modules_view AS
 SELECT da_product_modules.dpm_product_no,
    da_product_modules.dpm_module_no,
    da_product_modules.t_stamp
   FROM public.da_product_modules
  WHERE ((da_product_modules.dpm_product_no)::text IN ( SELECT dtag_da_product_view.dp_product_no
           FROM public.dtag_da_product_view));


--
-- Name: dtag_da_product_sas_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_product_sas_view AS
 SELECT da_product_sas.dps_product_no,
    da_product_sas.dps_sa_no,
    da_product_sas.dps_kg,
    da_product_sas.t_stamp,
    da_product_sas.dps_source
   FROM public.da_product_sas
  WHERE ((da_product_sas.dps_product_no)::text IN ( SELECT dtag_da_product_view.dp_product_no
           FROM public.dtag_da_product_view));


--
-- Name: dtag_da_replcae_part_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_replcae_part_view AS
 SELECT da_replace_part.drp_vari,
    da_replace_part.drp_ver,
    da_replace_part.drp_lfdnr,
    da_replace_part.drp_seqno,
    da_replace_part.drp_replace_matnr,
    da_replace_part.drp_replace_lfdnr,
    da_replace_part.drp_replace_rfmea,
    da_replace_part.drp_replace_rfmen,
    da_replace_part.t_stamp,
    da_replace_part.drp_source,
    da_replace_part.drp_status,
    da_replace_part.drp_source_guid,
    da_replace_part.drp_replace_source_guid
   FROM public.da_replace_part
  WHERE ((da_replace_part.drp_vari)::text IN ( SELECT dtag_da_module_view.dm_module_no
           FROM public.dtag_da_module_view));


--
-- Name: dtag_da_response_data_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_response_data_view AS
 SELECT da_response_data.drd_factory,
    da_response_data.drd_series_no,
    da_response_data.drd_aa,
    da_response_data.drd_bmaa,
    da_response_data.drd_pem,
    da_response_data.drd_adat,
    da_response_data.drd_ident,
    da_response_data.drd_as_data,
    da_response_data.drd_steering,
    da_response_data.drd_text,
    da_response_data.drd_agg_type,
    da_response_data.drd_valid,
    da_response_data.drd_source,
    da_response_data.drd_whc,
    da_response_data.drd_type,
    da_response_data.drd_status,
    da_response_data.t_stamp
   FROM public.da_response_data
  WHERE ((da_response_data.drd_series_no)::text = ''::text);


--
-- Name: dwarray; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dwarray (
    dwa_feld character varying(40) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwa_arrayid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwa_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dwa_token character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.dwarray ALTER COLUMN dwa_arrayid SET STATISTICS 500;
ALTER TABLE ONLY public.dwarray ALTER COLUMN dwa_token SET STATISTICS 250;


--
-- Name: katalog; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.katalog (
    k_vari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_sach character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_sver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_art character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_pos character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_ebene character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_mver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_menge character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_mengeart character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_bestflag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_seqnr character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_bmk character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_codes text COLLATE pg_catalog."en-US-x-icu",
    k_minusparts character varying(400) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_hierarchy character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_steering character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_source_type character varying(2) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_source_context character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_source_ref1 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_source_ref2 character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_source_guid character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_product_grp character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_sa_validity character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_model_validity character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_aa character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_gearbox_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_etz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_virtual_mat_type character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_ww character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_ww_extra_parts character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_fail_loclist character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_as_code character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_acc_code character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_pclasses_validity text COLLATE pg_catalog."en-US-x-icu",
    k_eval_pem_from character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_eval_pem_to character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_codes_const text COLLATE pg_catalog."en-US-x-icu",
    k_hierarchy_const character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_menge_const character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_omit character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_etkz character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_only_model_filter character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_event_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_event_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_event_from_const character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_event_to_const character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_codes_reduced text COLLATE pg_catalog."en-US-x-icu",
    k_datefrom character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_dateto character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_min_kem_date_from character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_max_kem_date_to character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_use_primus_successor character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_copy_vari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_copy_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_copy_date character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_psk_variant_validity character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_auto_created character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_entry_locked character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_country_validity character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_spec_validity character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_was_auto_created character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.katalog ALTER COLUMN k_vari SET STATISTICS 250;
ALTER TABLE ONLY public.katalog ALTER COLUMN k_sach SET STATISTICS 250;
ALTER TABLE ONLY public.katalog ALTER COLUMN k_matnr SET STATISTICS 500;
ALTER TABLE ONLY public.katalog ALTER COLUMN k_seqnr SET STATISTICS 250;
ALTER TABLE ONLY public.katalog ALTER COLUMN k_source_guid SET STATISTICS 1000;
ALTER TABLE ONLY public.katalog ALTER COLUMN k_sa_validity SET STATISTICS 500;
ALTER TABLE ONLY public.katalog ALTER COLUMN k_model_validity SET STATISTICS 500;
ALTER TABLE ONLY public.katalog ALTER COLUMN k_datefrom SET STATISTICS 250;
ALTER TABLE ONLY public.katalog ALTER COLUMN k_dateto SET STATISTICS 250;
ALTER TABLE ONLY public.katalog ALTER COLUMN k_min_kem_date_from SET STATISTICS 250;


--
-- Name: dtag_da_sa_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_sa_view AS
 SELECT da_sa.ds_sa,
    da_sa.ds_desc,
    da_sa.ds_edat,
    da_sa.ds_adat,
    da_sa.ds_codes,
    da_sa.ds_source,
    da_sa.t_stamp,
    da_sa.ds_not_docu_relevant,
    da_sa.ds_const_desc,
    da_sa.ds_const_sa
   FROM public.da_sa
  WHERE ((da_sa.ds_sa)::text IN ( SELECT substr((dwarray.dwa_token)::text, 1, (length((dwarray.dwa_token)::text) - 2)) AS sa
           FROM public.dwarray
          WHERE (((dwarray.dwa_feld)::text = 'KATALOG.K_SA_VALIDITY'::text) AND ((dwarray.dwa_arrayid)::text IN ( SELECT katalog.k_sa_validity
                   FROM public.katalog
                  WHERE ((katalog.k_vari)::text IN ( SELECT dtag_da_module_view.dm_module_no
                           FROM public.dtag_da_module_view)))))));


--
-- Name: dtag_da_saa_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_saa_view AS
 SELECT da_saa.ds_saa,
    da_saa.ds_desc,
    da_saa.ds_const_desc,
    da_saa.ds_desc_extended,
    da_saa.ds_remark,
    da_saa.ds_rev_from,
    da_saa.ds_edat,
    da_saa.ds_adat,
    da_saa.ds_connected_sas,
    da_saa.ds_source,
    da_saa.t_stamp,
    da_saa.ds_kg,
    da_saa.ds_saa_ref,
    da_saa.ds_const_saa,
    dwa.dwa_token
   FROM (public.da_saa
     LEFT JOIN ( SELECT dwarray.dwa_token
           FROM public.dwarray
          WHERE (((dwarray.dwa_feld)::text = 'KATALOG.K_SA_VALIDITY'::text) AND ((dwarray.dwa_arrayid)::text IN ( SELECT katalog.k_sa_validity
                   FROM public.katalog
                  WHERE ((katalog.k_vari)::text IN ( SELECT dtag_da_module_view.dm_module_no
                           FROM public.dtag_da_module_view)))))) dwa ON (((da_saa.ds_saa)::text = (dwa.dwa_token)::text)));


--
-- Name: dtag_da_top_tus_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_top_tus_view AS
 SELECT da_top_tus.dtt_product_no,
    da_top_tus.dtt_country_code,
    da_top_tus.dtt_kg,
    da_top_tus.dtt_tu,
    da_top_tus.dtt_rank,
    da_top_tus.t_stamp
   FROM (public.da_top_tus
     JOIN ( SELECT da_product_1.dp_product_no,
            da_product_1.dp_structuring_type,
            da_product_1.dp_title,
            da_product_1.dp_picture,
            da_product_1.dp_product_grp,
            da_product_1.dp_aggregate_type,
            da_product_1.dp_assortment_classes,
            da_product_1.dp_docu_method,
            da_product_1.dp_product_visible,
            da_product_1.dp_kz_delta,
            da_product_1.dp_migration,
            da_product_1.dp_migration_date,
            da_product_1.dp_dataset_date,
            da_product_1.dp_source,
            da_product_1.dp_asproduct_classes,
            da_product_1.dp_comment,
            da_product_1.dp_series_ref,
            da_product_1.dp_is_special_cat,
            da_product_1.dp_aps_remark,
            da_product_1.dp_aps_code,
            da_product_1.dp_aps_from_idents,
            da_product_1.dp_aps_to_idents,
            da_product_1.dp_ident_class_old,
            da_product_1.dp_epc_relevant,
            da_product_1.dp_valid_countries,
            da_product_1.dp_invalid_countries,
            da_product_1.dp_brand,
            da_product_1.dp_second_parts_enabled,
            da_product_1.dp_ttz_filter,
            da_product_1.dp_scoring_with_mcodes,
            da_product_1.dp_disabled_filters,
            da_product_1.dp_modification_timestamp,
            da_product_1.dp_show_sas,
            da_product_1.dp_cab_fallback,
            da_product_1.t_stamp,
            da_product_1.dp_no_primus_hints,
            da_product_1.dp_psk,
            da_product_1.dp_use_svgs,
            da_product_1.dp_prefer_svg,
            da_product_1.dp_ident_factory_filtering,
            da_product_1.dp_full_language_support,
            da_product_1.dp_es_export_timestamp,
            da_product_1.dp_dialog_pos_check,
            da_product_1.dp_supplier_no,
            da_product_1.dp_car_perspective,
            da_product_1.dp_use_factory,
            da_product_1.dp_connect_data_visible,
            da_product_1.dp_fins
           FROM public.da_product da_product_1
          WHERE ((da_product_1.dp_asproduct_classes ~~ '%<SOE>I</SOE>%'::text) OR (da_product_1.dp_asproduct_classes ~~ '%<SOE>K</SOE>%'::text) OR (da_product_1.dp_asproduct_classes ~~ '%<SOE>L</SOE>%'::text) OR (da_product_1.dp_asproduct_classes ~~ '%<SOE>O</SOE>%'::text) OR (da_product_1.dp_asproduct_classes ~~ '%<SOE>U</SOE>%'::text))) da_product ON (((da_product.dp_product_no)::text = (da_top_tus.dtt_product_no)::text)));


--
-- Name: dtag_da_vin_model_mapping_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_da_vin_model_mapping_view AS
 SELECT da_vin_model_mapping.dvm_vin_prefix,
    da_vin_model_mapping.dvm_model_prefix,
    da_vin_model_mapping.t_stamp
   FROM public.da_vin_model_mapping
  WHERE ((da_vin_model_mapping.dvm_model_prefix)::text IN ( SELECT substr((da_product_models.dpm_model_no)::text, 2, 4) AS substr
           FROM (public.da_product_models
             JOIN ( SELECT dtag_da_product_view.dp_product_no,
                    dtag_da_product_view.dp_structuring_type,
                    dtag_da_product_view.dp_title,
                    dtag_da_product_view.dp_picture,
                    dtag_da_product_view.dp_product_grp,
                    dtag_da_product_view.dp_aggregate_type,
                    dtag_da_product_view.dp_assortment_classes,
                    dtag_da_product_view.dp_docu_method,
                    dtag_da_product_view.dp_product_visible,
                    dtag_da_product_view.dp_kz_delta,
                    dtag_da_product_view.dp_migration,
                    dtag_da_product_view.dp_migration_date,
                    dtag_da_product_view.dp_dataset_date,
                    dtag_da_product_view.dp_source,
                    dtag_da_product_view.dp_asproduct_classes,
                    dtag_da_product_view.dp_comment,
                    dtag_da_product_view.dp_series_ref,
                    dtag_da_product_view.dp_is_special_cat,
                    dtag_da_product_view.dp_aps_remark,
                    dtag_da_product_view.dp_aps_code,
                    dtag_da_product_view.dp_aps_from_idents,
                    dtag_da_product_view.dp_aps_to_idents,
                    dtag_da_product_view.dp_ident_class_old,
                    dtag_da_product_view.dp_epc_relevant,
                    dtag_da_product_view.dp_valid_countries,
                    dtag_da_product_view.dp_invalid_countries,
                    dtag_da_product_view.dp_brand,
                    dtag_da_product_view.dp_second_parts_enabled,
                    dtag_da_product_view.dp_ttz_filter,
                    dtag_da_product_view.dp_scoring_with_mcodes,
                    dtag_da_product_view.dp_disabled_filters,
                    dtag_da_product_view.dp_modification_timestamp,
                    dtag_da_product_view.dp_show_sas,
                    dtag_da_product_view.dp_cab_fallback,
                    dtag_da_product_view.t_stamp,
                    dtag_da_product_view.dp_no_primus_hints,
                    dtag_da_product_view.dp_psk,
                    dtag_da_product_view.dp_use_svgs,
                    dtag_da_product_view.dp_prefer_svg,
                    dtag_da_product_view.dp_ident_factory_filtering,
                    dtag_da_product_view.dp_full_language_support,
                    dtag_da_product_view.dp_es_export_timestamp,
                    dtag_da_product_view.dp_dialog_pos_check,
                    dtag_da_product_view.dp_supplier_no,
                    dtag_da_product_view.dp_car_perspective,
                    dtag_da_product_view.dp_use_factory,
                    dtag_da_product_view.dp_connect_data_visible,
                    dtag_da_product_view.dp_fins
                   FROM public.dtag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_product_models.dpm_product_no)::text)))));


--
-- Name: dtag_dwarray_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_dwarray_view AS
 SELECT dwarray.dwa_feld,
    dwarray.dwa_arrayid,
    dwarray.dwa_lfdnr,
    dwarray.dwa_token,
    dwarray.t_stamp
   FROM public.dwarray
  WHERE ((length((dwarray.dwa_arrayid)::text) >= 7) AND (substr((dwarray.dwa_arrayid)::text, 1, (length((dwarray.dwa_arrayid)::text) - 7)) IN ( SELECT dtag_da_module_view.dm_module_no
           FROM public.dtag_da_module_view)));


--
-- Name: images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.images (
    i_tiffname character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_blatt character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_images character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_pver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_katalog character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_imagedate character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_codes text COLLATE pg_catalog."en-US-x-icu",
    i_model_validity character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_saa_constkit_validity character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_event_from character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_event_to character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_psk_variant_validity character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_only_fin_visible character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_navigation_perspective character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.images ALTER COLUMN i_tiffname SET STATISTICS 250;
ALTER TABLE ONLY public.images ALTER COLUMN i_images SET STATISTICS 250;


--
-- Name: dtag_images_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_images_view AS
 SELECT images.i_tiffname,
    images.i_ver,
    images.i_blatt,
    images.i_images,
    images.i_pver,
    images.t_stamp,
    images.i_katalog,
    images.i_imagedate,
    images.i_codes,
    images.i_model_validity,
    images.i_saa_constkit_validity,
    images.i_event_from,
    images.i_event_to,
    images.i_psk_variant_validity,
    images.i_only_fin_visible,
    images.i_navigation_perspective
   FROM public.images
  WHERE ((images.i_tiffname)::text IN ( SELECT dtag_da_module_view.dm_module_no
           FROM public.dtag_da_module_view));


--
-- Name: dtag_katalog_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_katalog_view AS
 SELECT katalog.k_vari,
    katalog.k_ver,
    katalog.k_lfdnr,
    katalog.k_sach,
    katalog.k_sver,
    katalog.k_art,
    katalog.k_pos,
    katalog.k_ebene,
    katalog.k_matnr,
    katalog.k_mver,
    katalog.k_menge,
    katalog.k_mengeart,
    katalog.k_bestflag,
    katalog.t_stamp,
    katalog.k_seqnr,
    katalog.k_bmk,
    katalog.k_codes,
    katalog.k_minusparts,
    katalog.k_hierarchy,
    katalog.k_steering,
    katalog.k_source_type,
    katalog.k_source_context,
    katalog.k_source_ref1,
    katalog.k_source_ref2,
    katalog.k_source_guid,
    katalog.k_product_grp,
    katalog.k_sa_validity,
    katalog.k_model_validity,
    katalog.k_aa,
    katalog.k_gearbox_type,
    katalog.k_etz,
    katalog.k_virtual_mat_type,
    katalog.k_ww,
    katalog.k_ww_extra_parts,
    katalog.k_fail_loclist,
    katalog.k_as_code,
    katalog.k_acc_code,
    katalog.k_pclasses_validity,
    katalog.k_eval_pem_from,
    katalog.k_eval_pem_to,
    katalog.k_codes_const,
    katalog.k_hierarchy_const,
    katalog.k_menge_const,
    katalog.k_omit,
    katalog.k_etkz,
    katalog.k_only_model_filter,
    katalog.k_event_from,
    katalog.k_event_to,
    katalog.k_event_from_const,
    katalog.k_event_to_const,
    katalog.k_codes_reduced,
    katalog.k_datefrom,
    katalog.k_dateto,
    katalog.k_min_kem_date_from,
    katalog.k_max_kem_date_to,
    katalog.k_use_primus_successor,
    katalog.k_copy_vari,
    katalog.k_copy_lfdnr,
    katalog.k_copy_date,
    katalog.k_psk_variant_validity,
    katalog.k_auto_created,
    katalog.k_entry_locked,
    katalog.k_country_validity,
    katalog.k_spec_validity,
    katalog.k_was_auto_created
   FROM public.katalog
  WHERE ((katalog.k_vari)::text IN ( SELECT dtag_da_module_view.dm_module_no
           FROM public.dtag_da_module_view));


--
-- Name: links; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.links (
    l_images character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_sprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_usage character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_koord_l character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_koord_o character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_koord_r character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_koord_u character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_art character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_text character varying(15) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_textver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    l_extinfo text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.links ALTER COLUMN l_images SET STATISTICS 250;


--
-- Name: dtag_links_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_links_view AS
 SELECT links.l_images,
    links.l_ver,
    links.l_lfdnr,
    links.l_sprach,
    links.l_usage,
    links.l_koord_l,
    links.l_koord_o,
    links.l_koord_r,
    links.l_koord_u,
    links.l_art,
    links.l_text,
    links.l_textver,
    links.l_extinfo,
    links.t_stamp
   FROM public.links
  WHERE ((links.l_images)::text IN ( SELECT images.i_images
           FROM public.images
          WHERE ((images.i_tiffname)::text IN ( SELECT dtag_da_module_view.dm_module_no
                   FROM public.dtag_da_module_view))));


--
-- Name: mat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mat (
    m_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_textnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_bestnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_bestflag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_status character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_armored_ind character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_as_es_1 character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_as_es_2 character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_assemblysign character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_base_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_brand character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_certrel character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_change_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_china_ind character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_const_desc character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_docreq character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_esd_ind character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_imagedate character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_imagestate character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_is_deleted character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_layout_flag character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_materialfinitestate character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_nato_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_noteone character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_notetwo character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_quantunit character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_refser character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_relatedpic character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_releasestate character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_reman_ind character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_securitysign character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_securitysign_repair character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_shelf_life character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_source text COLLATE pg_catalog."en-US-x-icu",
    m_state character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_svhc_ind character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_theftrel character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_theftrelinfo character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_variant_sign character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_vedocsign character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_weightcalc character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_weightprog character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_weightreal character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_addtext character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_verksnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_factory_ids character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_last_modified character varying(24) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_market_etkz bytea,
    m_internal_text character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_basket_sign character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_etkz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_etkz_old character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_assembly character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_etkz_mbs character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_addtext_edited character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_etkz_ctt character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_image_available character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_psk_material character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_psk_supplier_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_psk_manufacturer_no character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_psk_supplier_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_psk_manufacturer_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_psk_image_no_extern character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_psk_remark character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_weight character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_length character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_width character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_height character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_volume character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_matnr_mbag character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_matnr_dtag character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_hazardous_goods_indicator character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_partno_basic character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_partno_shortblock character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_partno_longblock character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    m_partno_longblock_plus character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.mat ALTER COLUMN m_matnr SET STATISTICS 500;
ALTER TABLE ONLY public.mat ALTER COLUMN m_textnr SET STATISTICS 250;
ALTER TABLE ONLY public.mat ALTER COLUMN m_bestnr SET STATISTICS 500;
ALTER TABLE ONLY public.mat ALTER COLUMN m_base_matnr SET STATISTICS 500;
ALTER TABLE ONLY public.mat ALTER COLUMN m_const_desc SET STATISTICS 500;
ALTER TABLE ONLY public.mat ALTER COLUMN m_noteone SET STATISTICS 500;
ALTER TABLE ONLY public.mat ALTER COLUMN m_refser SET STATISTICS 250;
ALTER TABLE ONLY public.mat ALTER COLUMN m_last_modified SET STATISTICS 500;
ALTER TABLE ONLY public.mat ALTER COLUMN m_matnr_mbag SET STATISTICS 500;
ALTER TABLE ONLY public.mat ALTER COLUMN m_matnr_dtag SET STATISTICS 500;


--
-- Name: dtag_mat_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_mat_view AS
 SELECT mat.m_matnr,
    mat.m_ver,
    mat.m_textnr,
    mat.m_bestnr,
    mat.m_bestflag,
    mat.m_status,
    mat.t_stamp,
    mat.m_armored_ind,
    mat.m_as_es_1,
    mat.m_as_es_2,
    mat.m_assemblysign,
    mat.m_base_matnr,
    mat.m_brand,
    mat.m_certrel,
    mat.m_change_desc,
    mat.m_china_ind,
    mat.m_const_desc,
    mat.m_docreq,
    mat.m_esd_ind,
    mat.m_imagedate,
    mat.m_imagestate,
    mat.m_is_deleted,
    mat.m_layout_flag,
    mat.m_materialfinitestate,
    mat.m_nato_no,
    mat.m_noteone,
    mat.m_notetwo,
    mat.m_quantunit,
    mat.m_refser,
    mat.m_relatedpic,
    mat.m_releasestate,
    mat.m_reman_ind,
    mat.m_securitysign,
    mat.m_securitysign_repair,
    mat.m_shelf_life,
    mat.m_source,
    mat.m_state,
    mat.m_svhc_ind,
    mat.m_theftrel,
    mat.m_theftrelinfo,
    mat.m_variant_sign,
    mat.m_vedocsign,
    mat.m_weightcalc,
    mat.m_weightprog,
    mat.m_weightreal,
    mat.m_addtext,
    mat.m_verksnr,
    mat.m_factory_ids,
    mat.m_last_modified,
    mat.m_market_etkz,
    mat.m_internal_text,
    mat.m_basket_sign,
    mat.m_etkz,
    mat.m_etkz_old,
    mat.m_assembly,
    mat.m_etkz_mbs,
    mat.m_addtext_edited,
    mat.m_etkz_ctt,
    mat.m_image_available,
    mat.m_psk_material,
    mat.m_psk_supplier_no,
    mat.m_psk_manufacturer_no,
    mat.m_psk_supplier_matnr,
    mat.m_psk_manufacturer_matnr,
    mat.m_psk_image_no_extern,
    mat.m_psk_remark,
    mat.m_weight,
    mat.m_length,
    mat.m_width,
    mat.m_height,
    mat.m_volume,
    mat.m_matnr_mbag,
    mat.m_matnr_dtag,
    mat.m_hazardous_goods_indicator,
    mat.m_partno_basic,
    mat.m_partno_shortblock,
    mat.m_partno_longblock,
    mat.m_partno_longblock_plus
   FROM public.mat
  WHERE (substr((mat.m_matnr)::text, 1, 10) IN ( SELECT substr((katalog.k_matnr)::text, 1, 10) AS substr
           FROM public.katalog
          WHERE ((katalog.k_vari)::text IN ( SELECT dtag_da_module_view.dm_module_no
                   FROM public.dtag_da_module_view))
        UNION
         SELECT substr((da_replace_part.drp_replace_matnr)::text, 1, 10) AS substr
           FROM public.da_replace_part
          WHERE ((da_replace_part.drp_vari)::text IN ( SELECT dtag_da_module_view.dm_module_no
                   FROM public.dtag_da_module_view))
        UNION
         SELECT substr((da_include_part.dip_include_matnr)::text, 1, 10) AS substr
           FROM public.da_include_part
          WHERE ((da_include_part.dip_vari)::text IN ( SELECT dtag_da_module_view.dm_module_no
                   FROM public.dtag_da_module_view))));


--
-- Name: pool; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pool (
    p_images character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_sprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_usage character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_data bytea,
    p_imgtype character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_aspectratio integer DEFAULT 0 NOT NULL,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_ratio integer DEFAULT 0 NOT NULL,
    p_isdraft character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_lastdate character varying(15) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_importdate character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_preview_data bytea,
    p_preview_imgtype character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_validity_scope character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.pool ALTER COLUMN p_images SET STATISTICS 250;


--
-- Name: dtag_pool_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_pool_view AS
 SELECT pool.p_images,
    pool.p_ver,
    pool.p_sprach,
    pool.p_usage,
    pool.p_data,
    pool.p_imgtype,
    pool.p_aspectratio,
    pool.t_stamp,
    pool.p_ratio,
    pool.p_isdraft,
    pool.p_lastdate,
    pool.p_importdate,
    pool.p_preview_data,
    pool.p_preview_imgtype,
    pool.p_validity_scope
   FROM public.pool
  WHERE ((pool.p_images)::text IN ( SELECT images.i_images
           FROM public.images
          WHERE ((images.i_tiffname)::text IN ( SELECT dtag_da_module_view.dm_module_no
                   FROM public.dtag_da_module_view))));


--
-- Name: poolentry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.poolentry (
    pe_images character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pe_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    pe_bem character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.poolentry ALTER COLUMN pe_images SET STATISTICS 250;


--
-- Name: dtag_poolentry_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_poolentry_view AS
 SELECT poolentry.pe_images,
    poolentry.pe_ver,
    poolentry.pe_bem,
    poolentry.t_stamp
   FROM public.poolentry
  WHERE ((poolentry.pe_images)::text IN ( SELECT images.i_images
           FROM public.images
          WHERE ((images.i_tiffname)::text IN ( SELECT dtag_da_module_view.dm_module_no
                   FROM public.dtag_da_module_view))));


--
-- Name: sprache; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sprache (
    s_feld character varying(40) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_sprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_textnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_benenn character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_textid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_benenn_lang text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);
ALTER TABLE ONLY public.sprache ALTER COLUMN s_textnr SET STATISTICS 1000;
ALTER TABLE ONLY public.sprache ALTER COLUMN s_benenn SET STATISTICS 1000;
ALTER TABLE ONLY public.sprache ALTER COLUMN s_textid SET STATISTICS 500;


--
-- Name: dtag_sprache_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.dtag_sprache_view AS
 SELECT sprache.s_feld,
    sprache.s_sprach,
    sprache.s_textnr,
    sprache.s_benenn,
    sprache.s_textid,
    sprache.s_benenn_lang,
    sprache.t_stamp
   FROM public.sprache;


--
-- Name: econnections; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.econnections (
    ec_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ec_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ec_itemtype character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ec_itemid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ec_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ec_desttype character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ec_destitem character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ehotspot; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ehotspot (
    eh_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_sheet character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_left character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_top character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_right character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_bottom character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_itemtype character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_itemid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_linktype character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_destsheet character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_destzoom character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_desttop character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_destleft character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_description character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: eitemdata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eitemdata (
    ed_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_itemtype character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_itemid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_key character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_value character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_sort character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: eitems; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eitems (
    ei_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_itemtype character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_itemid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_sort character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_parenttype character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_parentid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_mainsheet character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_mainid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_name character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_nodecaption character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_description character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ei_issubitem character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: elinks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.elinks (
    el_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    el_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    el_itemtype character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    el_itemid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    el_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    el_destsheet character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    el_destid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: emechlink; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.emechlink (
    em_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    em_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    em_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    em_kvari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    em_kver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: enum; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enum (
    e_name character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    e_token character varying(40) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    e_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    e_icondata bytea,
    e_showicon character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: enumlink; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enumlink (
    e_feld character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    e_value character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    e_default character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: epartdata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.epartdata (
    ed_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_partver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_key character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_value character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ed_sort character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: epartlink; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.epartlink (
    ep_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_itemtype character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_itemid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_partver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_quant character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: eparts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eparts (
    ep_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_partno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_partver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_visiblepartno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_description character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_linkpartno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_linkpartnover character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_orderno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ep_replaceddescription character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: eschemaentry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eschemaentry (
    eh_schemaentry character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_schemaentryver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_lang character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: eschemahead; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eschemahead (
    eh_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_title character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_dataver character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    eh_langs character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: esheet; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.esheet (
    es_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_sheet character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_title character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_type character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_name character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_data bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: estruct; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.estruct (
    es_key character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_keyver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_keydest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_keydestver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_destschemaentry character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_destschemaentryver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    es_title character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: etrans; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.etrans (
    et_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    et_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    et_textid character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    et_lang character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    et_text character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: etree; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.etree (
    et_schema character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    et_schemaver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    et_key character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    et_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    et_destkey character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    et_destsheet character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    et_title character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: export_dialog; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.export_dialog AS
 SELECT k.baureihe,
    count(*) AS anzahl_dialog_pv
   FROM ( SELECT DISTINCT substr((katalog.k_source_context)::text, 1, (length((katalog.k_source_context)::text) - 9)) AS baureihe,
            katalog.k_source_guid AS pv
           FROM (public.katalog
             LEFT JOIN public.da_omitted_parts ON (((katalog.k_matnr)::text = (da_omitted_parts.da_op_partno)::text)))
          WHERE ((da_omitted_parts.da_op_partno IS NULL) AND ((katalog.k_source_type)::text = 'D'::text) AND ((katalog.k_source_guid)::text <> ''::text) AND (length((katalog.k_source_context)::text) >= 9))) k
  GROUP BY k.baureihe
  ORDER BY k.baureihe;


--
-- Name: export_ki_predict; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.export_ki_predict AS
 SELECT da_dialog.dd_series_no AS baureihe,
    da_dialog.dd_hm AS hauptmodul,
    da_dialog.dd_m AS modul,
    da_dialog.dd_sm AS submodul,
    da_dialog.dd_pose AS pos,
    da_dialog.dd_posv AS posv,
    da_dialog.dd_etz AS etz,
    da_dialog.dd_etkz AS etk,
    da_dialog.dd_steering AS lenkung,
    da_dialog.dd_aa AS aa,
    da_dialog.dd_ww AS wahlweise,
    da_dialog.dd_sdata AS kem_datum_ab,
    da_dialog.dd_hierarchy AS strukturstufe_entwicklung,
    da_dialog.dd_partno AS teilenummer,
    da_dialog.dd_quantity AS menge,
    da_dialog.dd_codes AS coderegel,
    da_dialog.dd_event_from AS ereignis_ab,
    da_dialog.dd_event_to AS ereignis_bis,
    da_dialog.dd_guid AS guid,
    da_dialog.dd_fed AS federfuehrung,
    t1.m_textnr AS textid_as,
    s1.s_benenn AS benennung_as,
    s2.s_benenn AS benennung_entwicklung
   FROM ((((public.da_dialog
     LEFT JOIN public.mat t1 ON (((da_dialog.dd_partno)::text = (t1.m_matnr)::text)))
     LEFT JOIN public.sprache s1 ON ((((t1.m_textnr)::text = (s1.s_textnr)::text) AND ((s1.s_sprach)::text ~~ 'DE'::text) AND ((s1.s_feld)::text = 'MAT.M_TEXTNR'::text))))
     LEFT JOIN public.mat t2 ON (((da_dialog.dd_partno)::text = (t2.m_matnr)::text)))
     LEFT JOIN public.sprache s2 ON ((((t2.m_const_desc)::text = (s2.s_textnr)::text) AND ((s2.s_sprach)::text ~~ 'DE'::text) AND ((s2.s_feld)::text = 'MAT.M_CONST_DESC'::text))))
  WHERE ((da_dialog.dd_series_no)::text <> ALL ((ARRAY['C117'::character varying, 'C164'::character varying, 'C166'::character varying, 'C168'::character varying, 'C169'::character varying, 'C170'::character varying, 'C171'::character varying, 'C197'::character varying, 'C199'::character varying, 'C203'::character varying, 'C204'::character varying, 'C207'::character varying, 'C208'::character varying, 'C209'::character varying, 'C211'::character varying, 'C212'::character varying, 'C215'::character varying, 'C216'::character varying, 'C218'::character varying, 'C219'::character varying, 'C220'::character varying, 'C221'::character varying, 'C230'::character varying, 'C240'::character varying, 'C242'::character varying, 'C246'::character varying, 'C251'::character varying, 'C414'::character varying, 'C450'::character varying, 'C451'::character varying, 'C452'::character varying, 'C454'::character varying, 'C636'::character varying, 'C638'::character varying, 'C639'::character varying, 'C903'::character varying, 'C904'::character varying, 'C905'::character varying, 'D1119'::character varying, 'D1139'::character varying, 'D1229'::character varying, 'D1329'::character varying, 'D1349'::character varying, 'D1359'::character varying, 'D1529'::character varying, 'D1559'::character varying, 'D1569'::character varying, 'D1669'::character varying, 'D2609'::character varying, 'D2669'::character varying, 'D2718'::character varying, 'D2719'::character varying, 'D2729'::character varying, 'D2739'::character varying, 'D2759'::character varying, 'D2769'::character varying, 'D6129'::character varying, 'D6229'::character varying, 'D6269'::character varying, 'D6289'::character varying, 'D6299'::character varying, 'D6399'::character varying, 'D6409'::character varying, 'D6428'::character varying, 'D6429'::character varying, 'D6468'::character varying, 'D6469'::character varying, 'D6479'::character varying, 'D6489'::character varying, 'D6609'::character varying, 'D6689'::character varying, 'D70012'::character varying, 'D70073'::character varying, 'D71652'::character varying, 'D71660'::character varying, 'D71661'::character varying, 'D71662'::character varying, 'D71663'::character varying, 'D71664'::character varying, 'D71665'::character varying, 'D71666'::character varying, 'D71667'::character varying, 'D71675'::character varying, 'D71746'::character varying, 'D71747'::character varying, 'D71748'::character varying, 'D72255'::character varying, 'D7226'::character varying, 'D7227'::character varying, 'D7228'::character varying, ''::character varying])::text[]));


--
-- Name: export_ki_retrain; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.export_ki_retrain AS
 SELECT k.produkt,
    k.kg,
    k.tu,
    k.hotspot,
    k.teilenummer,
    k.menge,
    k.strukturstufe_as,
    k.baureihe,
    k.submodul,
    k.pos,
    k.pv,
    k.aa,
    k.lenkung,
    k.kem_datum_ab,
    k.ereignis_ab,
    k.ereignis_bis,
    k.dialog_id,
    k.astextid_as,
    k.benennung_as,
    k.benennung_entwicklung,
    k.iparts_tu_id,
    k.iparts_lfdnr
   FROM ( SELECT substr((katalog.k_vari)::text, 1, (length((katalog.k_vari)::text) - 13)) AS produkt,
            substr((katalog.k_vari)::text, (length((katalog.k_vari)::text) - 11), 2) AS kg,
            substr((katalog.k_vari)::text, (length((katalog.k_vari)::text) - 8), 3) AS tu,
            katalog.k_pos AS hotspot,
            katalog.k_matnr AS teilenummer,
            katalog.k_menge AS menge,
            katalog.k_hierarchy AS strukturstufe_as,
                CASE
                    WHEN (substr((katalog.k_source_guid)::text, 1, 1) = 'C'::text) THEN substr((katalog.k_source_guid)::text, 1, 4)
                    WHEN ((substr((katalog.k_source_guid)::text, 1, 1) = 'D'::text) AND (substr((katalog.k_source_guid)::text, 6, 1) = '|'::text)) THEN substr((katalog.k_source_guid)::text, 1, 5)
                    WHEN ((substr((katalog.k_source_guid)::text, 1, 1) = 'D'::text) AND (substr((katalog.k_source_guid)::text, 7, 1) = '|'::text)) THEN substr((katalog.k_source_guid)::text, 1, 6)
                    ELSE ' '::text
                END AS baureihe,
                CASE
                    WHEN (substr((katalog.k_source_guid)::text, 1, 1) = 'C'::text) THEN replace(substr((katalog.k_source_guid)::text, 6, 8), '|'::text, ''::text)
                    WHEN ((substr((katalog.k_source_guid)::text, 1, 1) = 'D'::text) AND (substr((katalog.k_source_guid)::text, 6, 1) = '|'::text)) THEN replace(substr((katalog.k_source_guid)::text, 7, 8), '|'::text, ''::text)
                    WHEN ((substr((katalog.k_source_guid)::text, 1, 1) = 'D'::text) AND (substr((katalog.k_source_guid)::text, 7, 1) = '|'::text)) THEN replace(substr((katalog.k_source_guid)::text, 8, 8), '|'::text, ''::text)
                    ELSE ' '::text
                END AS submodul,
            katalog.k_source_ref1 AS pos,
            katalog.k_source_ref2 AS pv,
            katalog.k_aa AS aa,
            katalog.k_steering AS lenkung,
            katalog.k_datefrom AS kem_datum_ab,
            katalog.k_event_from AS ereignis_ab,
            katalog.k_event_to AS ereignis_bis,
            katalog.k_source_guid AS dialog_id,
            t1.m_textnr AS astextid_as,
            s1.s_benenn AS benennung_as,
            s2.s_benenn AS benennung_entwicklung,
            katalog.k_vari AS iparts_tu_id,
            katalog.k_lfdnr AS iparts_lfdnr
           FROM ((((public.katalog
             LEFT JOIN public.mat t1 ON (((katalog.k_matnr)::text = (t1.m_matnr)::text)))
             LEFT JOIN public.sprache s1 ON ((((t1.m_textnr)::text = (s1.s_textnr)::text) AND ((s1.s_sprach)::text ~~ 'DE'::text) AND ((s1.s_feld)::text = 'MAT.M_TEXTNR'::text))))
             LEFT JOIN public.mat t2 ON (((katalog.k_matnr)::text = (t2.m_matnr)::text)))
             LEFT JOIN public.sprache s2 ON ((((t2.m_const_desc)::text = (s2.s_textnr)::text) AND ((s2.s_sprach)::text ~~ 'DE'::text) AND ((s2.s_feld)::text = 'MAT.M_CONST_DESC'::text))))
          WHERE (((katalog.k_source_guid)::text <> ''::text) AND ((katalog.k_source_type)::text = 'D'::text) AND ((katalog.k_source_ref1)::text <> ''::text) AND (length((katalog.k_vari)::text) >= 13))) k
  WHERE (k.baureihe <> ALL (ARRAY['C117'::text, 'C164'::text, 'C166'::text, 'C168'::text, 'C169'::text, 'C170'::text, 'C171'::text, 'C197'::text, 'C199'::text, 'C203'::text, 'C204'::text, 'C207'::text, 'C208'::text, 'C209'::text, 'C211'::text, 'C212'::text, 'C215'::text, 'C216'::text, 'C218'::text, 'C219'::text, 'C220'::text, 'C221'::text, 'C230'::text, 'C240'::text, 'C242'::text, 'C246'::text, 'C251'::text, 'C414'::text, 'C450'::text, 'C451'::text, 'C452'::text, 'C454'::text, 'C636'::text, 'C638'::text, 'C639'::text, 'C903'::text, 'C904'::text, 'C905'::text, 'D1119'::text, 'D1139'::text, 'D1229'::text, 'D1329'::text, 'D1349'::text, 'D1359'::text, 'D1529'::text, 'D1559'::text, 'D1569'::text, 'D1669'::text, 'D2609'::text, 'D2669'::text, 'D2718'::text, 'D2719'::text, 'D2729'::text, 'D2739'::text, 'D2759'::text, 'D2769'::text, 'D6129'::text, 'D6229'::text, 'D6269'::text, 'D6289'::text, 'D6299'::text, 'D6399'::text, 'D6409'::text, 'D6428'::text, 'D6429'::text, 'D6468'::text, 'D6469'::text, 'D6479'::text, 'D6489'::text, 'D6609'::text, 'D6689'::text, 'D70012'::text, 'D70073'::text, 'D71652'::text, 'D71660'::text, 'D71661'::text, 'D71662'::text, 'D71663'::text, 'D71664'::text, 'D71665'::text, 'D71666'::text, 'D71667'::text, 'D71675'::text, 'D71746'::text, 'D71747'::text, 'D71748'::text, 'D72255'::text, 'D7226'::text, 'D7227'::text, 'D7228'::text, ''::text]));


--
-- Name: export_lkw; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.export_lkw AS
 SELECT k.produkt,
    k.kg,
    count(*) AS anzahl_teilepositionen
   FROM ( SELECT substr((katalog.k_vari)::text, 1, (length((katalog.k_vari)::text) - 13)) AS produkt,
            substr((katalog.k_vari)::text, (length((katalog.k_vari)::text) - 11), 2) AS kg
           FROM public.katalog
          WHERE (((katalog.k_sach)::text = ''::text) AND ((katalog.k_vari)::text !~~ 'SA-Z'::text) AND (length((katalog.k_vari)::text) >= 13) AND (substr((katalog.k_vari)::text, 1, (length((katalog.k_vari)::text) - 13)) = ANY (ARRAY['002'::text, '527'::text, '529'::text, '530'::text, '539'::text, '01E'::text, '49E'::text, '84E'::text, '00D'::text, '01A'::text, '01C'::text, '01D'::text, '01F'::text, '01G'::text, '01H'::text, '01L'::text, '01M'::text, '01R'::text, '01S'::text, '01V'::text, '01W'::text, '01Z'::text, '04W'::text, '04X'::text, '04Y'::text, '05W'::text, '06G'::text, '06L'::text, '06M'::text, '06R'::text, '06S'::text, '06T'::text, '06U'::text, '06V'::text, '06W'::text, '06X'::text, '06Y'::text, '06Z'::text, '07G'::text, '07K'::text, '07M'::text, '07P'::text, '07R'::text, '07X'::text, '07Z'::text, '09F'::text, '11V'::text, '11W'::text, '11Y'::text, '13T'::text, '14B'::text, '14C'::text, '14Q'::text, '14T'::text, '14U'::text, '14V'::text, '14X'::text, '14Y'::text, '14Z'::text, '15B'::text, '20N'::text, '20U'::text, '21Z'::text, '24N'::text, '24W'::text, '25H'::text, '25S'::text, '26Y'::text, '29U'::text, '30L'::text, '30M'::text, '30N'::text, '30P'::text, '30Q'::text, '30R'::text, '30S'::text, '30U'::text, '30W'::text, '30X'::text, '30Y'::text, '30Z'::text, '31B'::text, '31D'::text, '33M'::text, '33N'::text, '33P'::text, '35S'::text, '35T'::text, '35U'::text, '35X'::text, '36A'::text, '36C'::text, '36F'::text, '36J'::text, '36S'::text, '36X'::text, '36Y'::text, '37S'::text, '37T'::text, '37U'::text, '39Q'::text, '39S'::text, '39V'::text, '48A'::text, '48B'::text, '48F'::text, '48K'::text, '48N'::text, '48R'::text, '48Z'::text, '49B'::text, '49C'::text, '49F'::text, '49H'::text, '49K'::text, '49M'::text, '49N'::text, '49P'::text, '49S'::text, '49T'::text, '49W'::text, '49X'::text, '49Y'::text, '50Z'::text, '52C'::text, '52J'::text, '52K'::text, '52L'::text, '52N'::text, '52S'::text, '52X'::text, '52Y'::text, '52Z'::text, '54H'::text, '55A'::text, '57Z'::text, '71S'::text, '71Y'::text, '72H'::text, '73P'::text, '74Q'::text, '75T'::text, '75V'::text, '80N'::text, '82T'::text, '84D'::text, '84Y'::text, '92A'::text, '92B'::text, '92H'::text, '92J'::text, '92K'::text, '92M'::text, '92U'::text, '92V'::text, '92W'::text, '92X'::text, '92Y'::text, '92Z'::text, '94D'::text, '94R'::text, '95W'::text, '96A'::text, 'B41'::text, 'B60'::text, 'B74'::text, 'C969'::text, 'C983'::text, 'D969'::text, 'E50'::text, 'E51'::text, 'E53'::text, 'F01'::text, 'F10'::text, 'F48'::text, 'F70'::text, 'F80'::text, 'F90'::text, 'F93'::text, 'F95'::text, 'G05'::text, 'G07'::text, 'G10'::text, 'G15'::text, 'G20'::text, 'G21'::text, 'G30'::text, 'G31'::text, 'G40'::text, 'G45'::text, 'G60'::text, 'G70'::text, 'G75'::text, 'G80'::text, 'G95'::text, 'H01'::text, 'H02'::text, 'H03'::text, 'H05'::text, 'H30'::text, 'H31'::text, 'H32'::text, 'I01'::text, 'I02'::text, 'L01'::text, 'L20'::text, 'L30'::text, 'L31'::text, 'L32'::text, 'M01'::text, 'M02'::text, 'M03'::text, 'M04'::text, 'M05'::text, 'M06'::text, 'M07'::text, 'M08'::text, 'M09'::text, 'M10'::text, 'M20'::text, 'M30'::text, 'M31'::text, 'M38'::text, 'M40'::text, 'M41'::text, 'M42'::text, 'M97'::text, 'R01'::text, 'R02'::text, 'R03'::text, 'R04'::text, 'R05'::text, 'R06'::text, 'R11'::text, 'R15'::text, 'R22'::text, 'R30'::text, 'R31'::text, 'R36'::text, 'R37'::text, 'R39'::text, 'R50'::text, 'R51'::text, 'R52'::text, 'R53'::text, 'R54'::text, 'S01'::text, 'S02'::text, 'S10'::text, 'T02'::text, 'U00'::text, 'U01'::text, 'U05'::text, 'U06'::text, 'U11'::text, 'U95'::text, 'V01'::text, 'V03'::text, 'V05'::text, 'V06'::text, 'V07'::text, 'V21'::text, 'V22'::text, 'V25'::text, 'V30'::text, 'V36'::text, 'V50'::text, 'V20'::text, 'D730'::text, 'V33'::text, 'V34'::text, 'V35'::text, '48C'::text, 'R20'::text, 'R34'::text, 'R35'::text, 'D746'::text, '49L'::text, 'R10'::text, 'L35'::text, 'T04'::text, 'T10'::text, 'T15'::text, '92E'::text, '94E'::text, '93E'::text, '97E'::text, '06J'::text, '06N'::text, '29S'::text, '52G'::text, '52H'::text, '52M'::text, '52Q'::text, '54A'::text, '82R'::text, '82U'::text, '82V'::text, '82X'::text, '82Z'::text, '92R'::text, '93B'::text, '93D'::text, '93F'::text, '93G'::text, '93H'::text, '93N'::text, '94F'::text, '95G'::text, '95H'::text, 'B16'::text, 'B30'::text, 'B31'::text, 'B40'::text, 'B56'::text, 'B80'::text, 'B90'::text, 'S50'::text, 'X30'::text, 'X35'::text, 'X36'::text, 'X43'::text, 'X46'::text, 'X47'::text])))) k
  GROUP BY k.produkt, k.kg
  ORDER BY k.produkt, k.kg;


--
-- Name: export_psk_bm; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.export_psk_bm AS
 SELECT da_models_aggs.dma_model_no AS fahrzeugbaumuster,
    da_models_aggs.dma_aggregate_no AS aggreagtebaumuster,
    fahrzeug_produkt.dpm_product_no AS fahrzeugprodukt,
    aggregate_produkt.dpm_product_no AS aggregateprodukt,
    fahrzeug_produkt.dpm_model_visible AS sichtbarkeit_fahrzeugbaumuster,
    aggregate_produkt.dpm_model_visible AS sichtbarkeit_aggregatebaumuster,
    f_produkt_stamm.dp_product_visible AS sichtbarkeit_fahrzeugprodukt,
    a_produkt_stamm.dp_product_visible AS sichtbarkeit_aggregateprodukt,
    f_produkt_stamm.dp_source
   FROM ((((public.da_models_aggs
     JOIN public.da_product_models fahrzeug_produkt ON (((fahrzeug_produkt.dpm_model_no)::text = (da_models_aggs.dma_model_no)::text)))
     JOIN public.da_product_models aggregate_produkt ON (((aggregate_produkt.dpm_model_no)::text = (da_models_aggs.dma_aggregate_no)::text)))
     JOIN public.da_product f_produkt_stamm ON (((f_produkt_stamm.dp_product_no)::text = (fahrzeug_produkt.dpm_product_no)::text)))
     JOIN public.da_product a_produkt_stamm ON (((a_produkt_stamm.dp_product_no)::text = (aggregate_produkt.dpm_product_no)::text)))
  WHERE (((f_produkt_stamm.dp_source)::text !~~ 'APP%'::text) AND ((a_produkt_stamm.dp_source)::text !~~ 'APP%'::text) AND ((fahrzeug_produkt.dpm_model_visible)::text = '1'::text) AND ((aggregate_produkt.dpm_model_visible)::text = '1'::text) AND ((f_produkt_stamm.dp_product_visible)::text = '1'::text) AND ((a_produkt_stamm.dp_product_visible)::text = '1'::text))
UNION
 SELECT ' '::character varying AS fahrzeugbaumuster,
    da_product_models.dpm_model_no AS aggreagtebaumuster,
    ' '::character varying AS fahrzeugprodukt,
    da_product_models.dpm_product_no AS aggregateprodukt,
    ' '::character varying AS sichtbarkeit_fahrzeugbaumuster,
    da_product_models.dpm_model_visible AS sichtbarkeit_aggregatebaumuster,
    ' '::character varying AS sichtbarkeit_fahrzeugprodukt,
    da_product.dp_product_visible AS sichtbarkeit_aggregateprodukt,
    da_product.dp_source
   FROM (public.da_product_models
     JOIN public.da_product ON (((da_product.dp_product_no)::text = (da_product_models.dpm_product_no)::text)))
  WHERE ((NOT ((da_product_models.dpm_model_no)::text IN ( SELECT da_models_aggs.dma_aggregate_no
           FROM public.da_models_aggs
          WHERE ((da_models_aggs.dma_model_no)::text <> ALL ((ARRAY['C000001'::character varying, 'C000006'::character varying])::text[]))))) AND ((da_product_models.dpm_model_no)::text ~~ 'D%'::text));


--
-- Name: export_psk_referenzen; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.export_psk_referenzen AS
 SELECT da_product_models.dpm_product_no,
    da_product_models.dpm_model_no,
    da_product_models.dpm_model_visible
   FROM (public.da_product_models
     JOIN public.da_product ON (((da_product_models.dpm_product_no)::text = (da_product.dp_product_no)::text)))
  WHERE ((da_product.dp_product_visible)::text = '1'::text);


--
-- Name: favorites; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.favorites (
    f_user_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    f_type character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    f_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    f_name character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    f_key text COLLATE pg_catalog."en-US-x-icu",
    f_tk_vari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    f_tk_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    f_dblang character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    f_doculang character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    f_filter text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: groupentry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.groupentry (
    g_gid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    g_uid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: groupfunc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.groupfunc (
    f_ugid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    f_fid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    f_allow character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: icons; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.icons (
    i_icon character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_data bytea,
    i_hint character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_dataprt bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    i_inetdata bytea
);


--
-- Name: internal_dbparams; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.internal_dbparams (
    dp_schema character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_key character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    dp_value character varying(1000) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: kapitel; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.kapitel (
    k_sprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_knoten character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_knver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_text character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_nr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_nrver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_dsprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_kap character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_seite character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_vknoten character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_vknver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_vsprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_fett character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_extview character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    k_seqnr character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: keyvalue; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.keyvalue (
    kv_key character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    kv_value character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: mbag_da_code_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_code_view AS
 SELECT da_code.dc_code_id,
    da_code.dc_series_no,
    da_code.dc_pgrp,
    da_code.dc_sdata,
    da_code.dc_source,
    da_code.dc_sdatb,
    da_code.dc_desc,
    da_code.t_stamp
   FROM public.da_code
  WHERE (((da_code.dc_pgrp)::text <> 'N'::text) AND ((da_code.dc_pgrp)::text <> 'U'::text));


--
-- Name: mbag_da_color_number_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_color_number_view AS
 SELECT da_color_number.dcn_color_no,
    da_color_number.dcn_sda,
    da_color_number.dcn_desc,
    da_color_number.dcn_edat,
    da_color_number.dcn_adat,
    da_color_number.dcn_source,
    da_color_number.t_stamp
   FROM public.da_color_number;


--
-- Name: mbag_da_colortable_content_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_colortable_content_view AS
 SELECT da_colortable_content.dctc_table_id,
    da_colortable_content.dctc_pos,
    da_colortable_content.dctc_sdata,
    da_colortable_content.dctc_sdatb,
    da_colortable_content.dctc_color_var,
    da_colortable_content.dctc_pgrp,
    da_colortable_content.dctc_code,
    da_colortable_content.dctc_etkz,
    da_colortable_content.dctc_code_as,
    da_colortable_content.dctc_source,
    da_colortable_content.dctc_eval_pem_from,
    da_colortable_content.dctc_eval_pem_to,
    da_colortable_content.dctc_status,
    da_colortable_content.dctc_event_from,
    da_colortable_content.dctc_event_to,
    da_colortable_content.dctc_event_from_as,
    da_colortable_content.dctc_event_to_as,
    da_colortable_content.t_stamp
   FROM public.da_colortable_content
  WHERE ((da_colortable_content.dctc_table_id)::text ~~ 'QFT%'::text);


--
-- Name: mbag_da_colortable_data_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_colortable_data_view AS
 SELECT da_colortable_data.dctd_table_id,
    da_colortable_data.dctd_desc,
    da_colortable_data.dctd_bem,
    da_colortable_data.dctd_fikz,
    da_colortable_data.dctd_valid_series,
    da_colortable_data.t_stamp,
    da_colortable_data.dctd_source
   FROM public.da_colortable_data
  WHERE ((da_colortable_data.dctd_table_id)::text ~~ 'QFT%'::text);


--
-- Name: mbag_da_colortable_factory_qft_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_colortable_factory_qft_view AS
 SELECT da_colortable_factory.dccf_table_id,
    da_colortable_factory.dccf_pos,
    da_colortable_factory.dccf_factory,
    da_colortable_factory.dccf_adat,
    da_colortable_factory.dccf_data_id,
    da_colortable_factory.dccf_sdata,
    da_colortable_factory.dccf_sdatb,
    da_colortable_factory.dccf_pema,
    da_colortable_factory.dccf_pemb,
    da_colortable_factory.dccf_pemta,
    da_colortable_factory.dccf_pemtb,
    da_colortable_factory.dccf_stca,
    da_colortable_factory.dccf_stcb,
    da_colortable_factory.dccf_source,
    da_colortable_factory.dccf_pos_source,
    da_colortable_factory.dccf_status,
    da_colortable_factory.dccf_event_from,
    da_colortable_factory.dccf_event_to,
    da_colortable_factory.t_stamp,
    da_colortable_factory.dccf_original_sdata,
    da_colortable_factory.dccf_is_deleted
   FROM public.da_colortable_factory
  WHERE ((da_colortable_factory.dccf_table_id)::text ~~ 'QFT%'::text);


--
-- Name: mbag_da_colortable_factory_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_colortable_factory_view AS
 SELECT da_colortable_factory.dccf_table_id,
    da_colortable_factory.dccf_pos,
    da_colortable_factory.dccf_factory,
    da_colortable_factory.dccf_adat,
    da_colortable_factory.dccf_data_id,
    da_colortable_factory.dccf_sdata,
    da_colortable_factory.dccf_sdatb,
    da_colortable_factory.dccf_pema,
    da_colortable_factory.dccf_pemb,
    da_colortable_factory.dccf_pemta,
    da_colortable_factory.dccf_pemtb,
    da_colortable_factory.dccf_stca,
    da_colortable_factory.dccf_stcb,
    da_colortable_factory.dccf_source,
    da_colortable_factory.dccf_pos_source,
    da_colortable_factory.dccf_status,
    da_colortable_factory.dccf_event_from,
    da_colortable_factory.dccf_event_to,
    da_colortable_factory.t_stamp,
    da_colortable_factory.dccf_original_sdata,
    da_colortable_factory.dccf_is_deleted
   FROM public.da_colortable_factory;


--
-- Name: mbag_da_colortable_part_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_colortable_part_view AS
 SELECT da_colortable_part.dctp_table_id,
    da_colortable_part.dctp_pos,
    da_colortable_part.dctp_sdata,
    da_colortable_part.dctp_sdatb,
    da_colortable_part.dctp_part,
    da_colortable_part.dctp_etkz,
    da_colortable_part.dctp_source,
    da_colortable_part.dctp_pos_source,
    da_colortable_part.dctp_status,
    da_colortable_part.t_stamp,
    da_colortable_part.dctp_eval_pem_from,
    da_colortable_part.dctp_eval_pem_to
   FROM public.da_colortable_part
  WHERE ((da_colortable_part.dctp_table_id)::text ~~ 'QFT%'::text);


--
-- Name: mbag_da_product_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_product_view AS
 SELECT da_product.dp_product_no,
    da_product.dp_structuring_type,
    da_product.dp_title,
    da_product.dp_picture,
    da_product.dp_product_grp,
    da_product.dp_aggregate_type,
    da_product.dp_assortment_classes,
    da_product.dp_docu_method,
    da_product.dp_product_visible,
    da_product.dp_kz_delta,
    da_product.dp_migration,
    da_product.dp_migration_date,
    da_product.dp_dataset_date,
    da_product.dp_source,
    da_product.dp_asproduct_classes,
    da_product.dp_comment,
    da_product.dp_series_ref,
    da_product.dp_is_special_cat,
    da_product.dp_aps_remark,
    da_product.dp_aps_code,
    da_product.dp_aps_from_idents,
    da_product.dp_aps_to_idents,
    da_product.dp_ident_class_old,
    da_product.dp_epc_relevant,
    da_product.dp_valid_countries,
    da_product.dp_invalid_countries,
    da_product.dp_brand,
    da_product.dp_second_parts_enabled,
    da_product.dp_ttz_filter,
    da_product.dp_scoring_with_mcodes,
    da_product.dp_disabled_filters,
    da_product.dp_modification_timestamp,
    da_product.dp_show_sas,
    da_product.dp_cab_fallback,
    da_product.t_stamp,
    da_product.dp_no_primus_hints,
    da_product.dp_psk,
    da_product.dp_use_svgs,
    da_product.dp_prefer_svg,
    da_product.dp_ident_factory_filtering,
    da_product.dp_full_language_support,
    da_product.dp_es_export_timestamp,
    da_product.dp_dialog_pos_check,
    da_product.dp_supplier_no,
    da_product.dp_car_perspective,
    da_product.dp_use_factory,
    da_product.dp_connect_data_visible,
    da_product.dp_fins
   FROM public.da_product
  WHERE ((da_product.dp_asproduct_classes ~~ '%<SOE>P</SOE>%'::text) OR (da_product.dp_asproduct_classes ~~ '%<SOE>T</SOE>%'::text) OR (da_product.dp_asproduct_classes ~~ '%<SOE>G</SOE>%'::text) OR (da_product.dp_asproduct_classes ~~ '%<SOE>F</SOE>%'::text));


--
-- Name: mbag_da_module_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_module_view AS
 SELECT da_module.dm_module_no,
    da_module.t_stamp,
    da_module.dm_docutype,
    da_module.dm_spring_filter,
    da_module.dm_variants_visible,
    da_module.dm_use_color_tablefn,
    da_module.dm_module_hidden,
    da_module.dm_spec,
    da_module.dm_pos_pic_check_inactive,
    da_module.dm_source_tu,
    da_module.dm_hotspot_pic_check_inactive,
    da_module.dm_zb_part_no_agg_type,
    da_module.dm_special_tu
   FROM public.da_module
  WHERE ((da_module.dm_module_no)::text IN ( SELECT da_product_modules.dpm_module_no
           FROM public.da_product_modules
          WHERE ((da_product_modules.dpm_product_no)::text IN ( SELECT mbag_da_product_view.dp_product_no
                   FROM public.mbag_da_product_view))
        UNION
         SELECT ('SA-'::text || (da_product_sas.dps_sa_no)::text)
           FROM public.da_product_sas
          WHERE ((da_product_sas.dps_product_no)::text IN ( SELECT mbag_da_product_view.dp_product_no
                   FROM public.mbag_da_product_view))));


--
-- Name: mbag_da_comb_text_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_comb_text_view AS
 SELECT da_comb_text.dct_module,
    da_comb_text.dct_modver,
    da_comb_text.dct_seqno,
    da_comb_text.dct_text_seqno,
    da_comb_text.dct_dict_text,
    da_comb_text.dct_text_neutral,
    da_comb_text.t_stamp,
    da_comb_text.dct_source_genvo
   FROM public.da_comb_text
  WHERE ((da_comb_text.dct_module)::text IN ( SELECT da_module.dm_module_no
           FROM public.da_module
          WHERE ((da_module.dm_module_no)::text IN ( SELECT mbag_da_module_view.dm_module_no
                   FROM public.mbag_da_module_view))));


--
-- Name: mbag_da_dict_meta_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_dict_meta_view AS
 SELECT da_dict_meta.da_dict_meta_txtkind_id,
    da_dict_meta.da_dict_meta_textid,
    da_dict_meta.da_dict_meta_foreignid,
    da_dict_meta.da_dict_meta_source,
    da_dict_meta.da_dict_meta_state,
    da_dict_meta.da_dict_meta_create,
    da_dict_meta.da_dict_meta_change,
    da_dict_meta.da_dict_meta_userid,
    da_dict_meta.da_dict_meta_dialogid,
    da_dict_meta.da_dict_meta_eldasid,
    da_dict_meta.t_stamp
   FROM public.da_dict_meta;


--
-- Name: mbag_da_factory_data_products_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_factory_data_products_view AS
 SELECT da_factory_data.dfd_guid,
    da_factory_data.dfd_factory,
    da_factory_data.dfd_spkz,
    da_factory_data.dfd_adat,
    da_factory_data.dfd_data_id,
    da_factory_data.dfd_seq_no,
    da_factory_data.dfd_product_grp,
    da_factory_data.dfd_series_no,
    da_factory_data.dfd_hm,
    da_factory_data.dfd_m,
    da_factory_data.dfd_sm,
    da_factory_data.dfd_pose,
    da_factory_data.dfd_posv,
    da_factory_data.dfd_ww,
    da_factory_data.dfd_et,
    da_factory_data.dfd_aa,
    da_factory_data.dfd_sdata,
    da_factory_data.dfd_pema,
    da_factory_data.dfd_pemb,
    da_factory_data.dfd_pemta,
    da_factory_data.dfd_pemtb,
    da_factory_data.dfd_crn,
    da_factory_data.dfd_stca,
    da_factory_data.dfd_stcb,
    da_factory_data.dfd_source,
    da_factory_data.dfd_fn_id,
    da_factory_data.dfd_status,
    da_factory_data.dfd_event_from,
    da_factory_data.dfd_event_to,
    da_factory_data.t_stamp,
    da_factory_data.dfd_linked
   FROM public.da_factory_data
  WHERE ((((da_factory_data.dfd_series_no)::text = ''::text) AND (length((da_factory_data.dfd_guid)::text) >= 19) AND (substr((da_factory_data.dfd_guid)::text, 1, (length((da_factory_data.dfd_guid)::text) - 19)) IN ( SELECT mbag_da_product_view.dp_product_no
           FROM public.mbag_da_product_view
          WHERE ((mbag_da_product_view.dp_series_ref)::text = ''::text)))) OR ((da_factory_data.dfd_series_no)::text <> ''::text));


--
-- Name: mbag_da_factory_data_sa_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_factory_data_sa_view AS
 SELECT da_factory_data.dfd_guid,
    da_factory_data.dfd_factory,
    da_factory_data.dfd_spkz,
    da_factory_data.dfd_adat,
    da_factory_data.dfd_data_id,
    da_factory_data.dfd_seq_no,
    da_factory_data.dfd_product_grp,
    da_factory_data.dfd_series_no,
    da_factory_data.dfd_hm,
    da_factory_data.dfd_m,
    da_factory_data.dfd_sm,
    da_factory_data.dfd_pose,
    da_factory_data.dfd_posv,
    da_factory_data.dfd_ww,
    da_factory_data.dfd_et,
    da_factory_data.dfd_aa,
    da_factory_data.dfd_sdata,
    da_factory_data.dfd_pema,
    da_factory_data.dfd_pemb,
    da_factory_data.dfd_pemta,
    da_factory_data.dfd_pemtb,
    da_factory_data.dfd_crn,
    da_factory_data.dfd_stca,
    da_factory_data.dfd_stcb,
    da_factory_data.dfd_source,
    da_factory_data.dfd_fn_id,
    da_factory_data.dfd_status,
    da_factory_data.dfd_event_from,
    da_factory_data.dfd_event_to,
    da_factory_data.t_stamp,
    da_factory_data.dfd_linked
   FROM public.da_factory_data
  WHERE ((length((da_factory_data.dfd_guid)::text) >= 6) AND (substr((da_factory_data.dfd_guid)::text, 1, (length((da_factory_data.dfd_guid)::text) - 6)) IN ( SELECT ('SA-'::text || (da_product_sas.dps_sa_no)::text)
           FROM public.da_product_sas
          WHERE ((da_product_sas.dps_product_no)::text IN ( SELECT mbag_da_product_view.dp_product_no
                   FROM public.mbag_da_product_view)))));


--
-- Name: mbag_da_fn_content_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_fn_content_view AS
 SELECT da_fn_content.dfnc_fnid,
    da_fn_content.dfnc_line_no,
    da_fn_content.dfnc_text,
    da_fn_content.dfnc_text_neutral,
    da_fn_content.t_stamp
   FROM public.da_fn_content;


--
-- Name: mbag_da_fn_kat_ref_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_fn_kat_ref_view AS
 SELECT da_fn_katalog_ref.dfnk_module,
    da_fn_katalog_ref.dfnk_modver,
    da_fn_katalog_ref.dfnk_seqno,
    da_fn_katalog_ref.dfnk_fnid,
    da_fn_katalog_ref.dfnk_fn_seqno,
    da_fn_katalog_ref.dfnk_fn_marked,
    da_fn_katalog_ref.dfnk_colortablefootnote,
    da_fn_katalog_ref.t_stamp
   FROM public.da_fn_katalog_ref
  WHERE ((da_fn_katalog_ref.dfnk_module)::text IN ( SELECT da_module.dm_module_no
           FROM public.da_module
          WHERE ((da_module.dm_module_no)::text IN ( SELECT mbag_da_module_view.dm_module_no
                   FROM public.mbag_da_module_view))));


--
-- Name: mbag_da_fn_mat_ref_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_fn_mat_ref_view AS
 SELECT da_fn_mat_ref.dfnm_matnr,
    da_fn_mat_ref.dfnm_fnid,
    da_fn_mat_ref.t_stamp,
    da_fn_mat_ref.dfnm_source
   FROM public.da_fn_mat_ref
  WHERE (((da_fn_mat_ref.dfnm_source)::text = 'IPARTS'::text) OR ((da_fn_mat_ref.dfnm_source)::text = 'DIALOG'::text));


--
-- Name: mbag_da_fn_saa_ref_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_fn_saa_ref_view AS
 SELECT da_fn_saa_ref.dfns_saa,
    da_fn_saa_ref.dfns_fnid,
    da_fn_saa_ref.dfns_fn_seqno,
    da_fn_saa_ref.t_stamp
   FROM public.da_fn_saa_ref
  WHERE (substr((da_fn_saa_ref.dfns_saa)::text, 1, 7) IN ( SELECT da_product_sas.dps_sa_no
           FROM public.da_product_sas
          WHERE ((da_product_sas.dps_product_no)::text IN ( SELECT mbag_da_product_view.dp_product_no
                   FROM public.mbag_da_product_view))));


--
-- Name: mbag_da_fn_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_fn_view AS
 SELECT da_fn.dfn_id,
    da_fn.dfn_name,
    da_fn.dfn_standard,
    da_fn.t_stamp,
    da_fn.dfn_type
   FROM public.da_fn;


--
-- Name: mbag_da_generic_install_location_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_generic_install_location_view AS
 SELECT da_generic_install_location.dgil_series,
    da_generic_install_location.dgil_hm,
    da_generic_install_location.dgil_m,
    da_generic_install_location.dgil_sm,
    da_generic_install_location.dgil_pose,
    da_generic_install_location.dgil_sda,
    da_generic_install_location.dgil_sdb,
    da_generic_install_location.dgil_sesi,
    da_generic_install_location.dgil_fed,
    da_generic_install_location.dgil_hierarchy,
    da_generic_install_location.dgil_pos_key,
    da_generic_install_location.dgil_mk_sign,
    da_generic_install_location.dgil_pet_sign,
    da_generic_install_location.dgil_pwk_sign,
    da_generic_install_location.dgil_ptk_sign,
    da_generic_install_location.dgil_info_text,
    da_generic_install_location.dgil_delete_sign,
    da_generic_install_location.dgil_split_sign,
    da_generic_install_location.dgil_gen_install_location,
    da_generic_install_location.t_stamp
   FROM public.da_generic_install_location;


--
-- Name: mbag_da_include_part_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_include_part_view AS
 SELECT da_include_part.dip_vari,
    da_include_part.dip_ver,
    da_include_part.dip_lfdnr,
    da_include_part.dip_replace_matnr,
    da_include_part.dip_replace_lfdnr,
    da_include_part.dip_seqno,
    da_include_part.dip_include_matnr,
    da_include_part.dip_include_quantity,
    da_include_part.t_stamp
   FROM public.da_include_part
  WHERE ((da_include_part.dip_vari)::text IN ( SELECT mbag_da_module_view.dm_module_no
           FROM public.mbag_da_module_view));


--
-- Name: mbag_da_kgtu_as_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_kgtu_as_view AS
 SELECT da_kgtu_as.da_dkm_product,
    da_kgtu_as.da_dkm_kg,
    da_kgtu_as.da_dkm_tu,
    da_kgtu_as.da_dkm_desc,
    da_kgtu_as.da_dkm_edat,
    da_kgtu_as.da_dkm_adat,
    da_kgtu_as.da_dkm_source,
    da_kgtu_as.t_stamp
   FROM public.da_kgtu_as
  WHERE ((da_kgtu_as.da_dkm_product)::text IN ( SELECT mbag_da_product_view.dp_product_no
           FROM public.mbag_da_product_view));


--
-- Name: mbag_da_model_aggs_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_model_aggs_view AS
 SELECT da_models_aggs.dma_model_no,
    da_models_aggs.dma_aggregate_no,
    da_models_aggs.dma_source,
    da_models_aggs.t_stamp
   FROM public.da_models_aggs
  WHERE ((da_models_aggs.dma_model_no)::text IN ( SELECT da_product_models.dpm_model_no
           FROM (public.da_product_models
             JOIN ( SELECT mbag_da_product_view.dp_product_no,
                    mbag_da_product_view.dp_structuring_type,
                    mbag_da_product_view.dp_title,
                    mbag_da_product_view.dp_picture,
                    mbag_da_product_view.dp_product_grp,
                    mbag_da_product_view.dp_aggregate_type,
                    mbag_da_product_view.dp_assortment_classes,
                    mbag_da_product_view.dp_docu_method,
                    mbag_da_product_view.dp_product_visible,
                    mbag_da_product_view.dp_kz_delta,
                    mbag_da_product_view.dp_migration,
                    mbag_da_product_view.dp_migration_date,
                    mbag_da_product_view.dp_dataset_date,
                    mbag_da_product_view.dp_source,
                    mbag_da_product_view.dp_asproduct_classes,
                    mbag_da_product_view.dp_comment,
                    mbag_da_product_view.dp_series_ref,
                    mbag_da_product_view.dp_is_special_cat,
                    mbag_da_product_view.dp_aps_remark,
                    mbag_da_product_view.dp_aps_code,
                    mbag_da_product_view.dp_aps_from_idents,
                    mbag_da_product_view.dp_aps_to_idents,
                    mbag_da_product_view.dp_ident_class_old,
                    mbag_da_product_view.dp_epc_relevant,
                    mbag_da_product_view.dp_valid_countries,
                    mbag_da_product_view.dp_invalid_countries,
                    mbag_da_product_view.dp_brand,
                    mbag_da_product_view.dp_second_parts_enabled,
                    mbag_da_product_view.dp_ttz_filter,
                    mbag_da_product_view.dp_scoring_with_mcodes,
                    mbag_da_product_view.dp_disabled_filters,
                    mbag_da_product_view.dp_modification_timestamp,
                    mbag_da_product_view.dp_show_sas,
                    mbag_da_product_view.dp_cab_fallback,
                    mbag_da_product_view.t_stamp,
                    mbag_da_product_view.dp_no_primus_hints,
                    mbag_da_product_view.dp_psk,
                    mbag_da_product_view.dp_use_svgs,
                    mbag_da_product_view.dp_prefer_svg,
                    mbag_da_product_view.dp_ident_factory_filtering,
                    mbag_da_product_view.dp_full_language_support,
                    mbag_da_product_view.dp_es_export_timestamp,
                    mbag_da_product_view.dp_dialog_pos_check,
                    mbag_da_product_view.dp_supplier_no,
                    mbag_da_product_view.dp_car_perspective,
                    mbag_da_product_view.dp_use_factory,
                    mbag_da_product_view.dp_connect_data_visible,
                    mbag_da_product_view.dp_fins
                   FROM public.mbag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_product_models.dpm_product_no)::text)))));


--
-- Name: mbag_da_model_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_model_view AS
 SELECT da_model.dm_model_no,
    da_model.dm_series_no,
    da_model.dm_name,
    da_model.dm_code,
    da_model.dm_horsepower,
    da_model.dm_kilowatts,
    da_model.dm_sales_title,
    da_model.dm_development_title,
    da_model.dm_drive_system,
    da_model.dm_engine_concept,
    da_model.dm_cylinder_count,
    da_model.dm_engine_kind,
    da_model.dm_aa,
    da_model.dm_steering,
    da_model.dm_product_grp,
    da_model.dm_data,
    da_model.dm_datb,
    da_model.dm_model_type,
    da_model.dm_source,
    da_model.dm_model_visible,
    da_model.dm_as_from,
    da_model.dm_as_to,
    da_model.dm_model_invalid,
    da_model.dm_comment,
    da_model.dm_techdata,
    da_model.dm_valid_from,
    da_model.dm_valid_to,
    da_model.dm_add_text,
    da_model.dm_manual_change,
    da_model.dm_const_model_no,
    da_model.t_stamp,
    da_model.dm_filter_relevant,
    da_model.dm_not_docu_relevant,
    da_model.dm_model_suffix
   FROM public.da_model
  WHERE ((da_model.dm_model_no)::text IN ( SELECT da_product_models.dpm_model_no
           FROM (public.da_product_models
             JOIN public.mbag_da_product_view ON (((mbag_da_product_view.dp_product_no)::text = (da_product_models.dpm_product_no)::text)))));


--
-- Name: mbag_da_module_cemat_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_module_cemat_view AS
 SELECT da_module_cemat.dmc_module_no,
    da_module_cemat.dmc_lfdnr,
    da_module_cemat.dmc_partno,
    da_module_cemat.dmc_einpas_hg,
    da_module_cemat.dmc_einpas_g,
    da_module_cemat.dmc_einpas_tu,
    da_module_cemat.dmc_versions,
    da_module_cemat.t_stamp
   FROM public.da_module_cemat
  WHERE ((da_module_cemat.dmc_module_no)::text IN ( SELECT mbag_da_module_view.dm_module_no
           FROM public.mbag_da_module_view));


--
-- Name: mbag_da_modules_einpas_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_modules_einpas_view AS
 SELECT da_modules_einpas.dme_product_no,
    da_modules_einpas.dme_module_no,
    da_modules_einpas.dme_lfdnr,
    da_modules_einpas.dme_einpas_hg,
    da_modules_einpas.dme_einpas_g,
    da_modules_einpas.dme_einpas_tu,
    da_modules_einpas.dme_source_kg,
    da_modules_einpas.dme_source_tu,
    da_modules_einpas.dme_source_hm,
    da_modules_einpas.dme_source_m,
    da_modules_einpas.dme_source_sm,
    da_modules_einpas.dme_sort,
    da_modules_einpas.t_stamp,
    da_modules_einpas.dme_saa_validity,
    da_modules_einpas.dme_code_validity,
    da_modules_einpas.dme_model_validity
   FROM (public.da_modules_einpas
     JOIN ( SELECT mbag_da_product_view.dp_product_no,
            mbag_da_product_view.dp_structuring_type,
            mbag_da_product_view.dp_title,
            mbag_da_product_view.dp_picture,
            mbag_da_product_view.dp_product_grp,
            mbag_da_product_view.dp_aggregate_type,
            mbag_da_product_view.dp_assortment_classes,
            mbag_da_product_view.dp_docu_method,
            mbag_da_product_view.dp_product_visible,
            mbag_da_product_view.dp_kz_delta,
            mbag_da_product_view.dp_migration,
            mbag_da_product_view.dp_migration_date,
            mbag_da_product_view.dp_dataset_date,
            mbag_da_product_view.dp_source,
            mbag_da_product_view.dp_asproduct_classes,
            mbag_da_product_view.dp_comment,
            mbag_da_product_view.dp_series_ref,
            mbag_da_product_view.dp_is_special_cat,
            mbag_da_product_view.dp_aps_remark,
            mbag_da_product_view.dp_aps_code,
            mbag_da_product_view.dp_aps_from_idents,
            mbag_da_product_view.dp_aps_to_idents,
            mbag_da_product_view.dp_ident_class_old,
            mbag_da_product_view.dp_epc_relevant,
            mbag_da_product_view.dp_valid_countries,
            mbag_da_product_view.dp_invalid_countries,
            mbag_da_product_view.dp_brand,
            mbag_da_product_view.dp_second_parts_enabled,
            mbag_da_product_view.dp_ttz_filter,
            mbag_da_product_view.dp_scoring_with_mcodes,
            mbag_da_product_view.dp_disabled_filters,
            mbag_da_product_view.dp_modification_timestamp,
            mbag_da_product_view.dp_show_sas,
            mbag_da_product_view.dp_cab_fallback,
            mbag_da_product_view.t_stamp,
            mbag_da_product_view.dp_no_primus_hints,
            mbag_da_product_view.dp_psk,
            mbag_da_product_view.dp_use_svgs,
            mbag_da_product_view.dp_prefer_svg,
            mbag_da_product_view.dp_ident_factory_filtering,
            mbag_da_product_view.dp_full_language_support,
            mbag_da_product_view.dp_es_export_timestamp,
            mbag_da_product_view.dp_dialog_pos_check,
            mbag_da_product_view.dp_supplier_no,
            mbag_da_product_view.dp_car_perspective,
            mbag_da_product_view.dp_use_factory,
            mbag_da_product_view.dp_connect_data_visible,
            mbag_da_product_view.dp_fins
           FROM public.mbag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_modules_einpas.dme_product_no)::text)));


--
-- Name: mbag_da_omitted_parts_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_omitted_parts_view AS
 SELECT da_omitted_parts.da_op_partno,
    da_omitted_parts.t_stamp
   FROM public.da_omitted_parts;


--
-- Name: mbag_da_primus_include_part_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_primus_include_part_view AS
 SELECT da_primus_include_part.pip_part_no,
    da_primus_include_part.pip_include_part_no,
    da_primus_include_part.pip_quantity,
    da_primus_include_part.t_stamp
   FROM public.da_primus_include_part;


--
-- Name: mbag_da_primus_replace_part_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_primus_replace_part_view AS
 SELECT da_primus_replace_part.prp_part_no,
    da_primus_replace_part.prp_successor_partno,
    da_primus_replace_part.prp_brand,
    da_primus_replace_part.prp_pss_code_forward,
    da_primus_replace_part.prp_pss_code_back,
    da_primus_replace_part.prp_pss_info_type,
    da_primus_replace_part.prp_lifecycle_state,
    da_primus_replace_part.t_stamp
   FROM public.da_primus_replace_part;


--
-- Name: mbag_da_product_factories_plant_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_product_factories_plant_view AS
 SELECT da_product_factories.dpf_product_no,
    da_product_factories.dpf_factory_no,
    da_product_factories.dpf_edat,
    da_product_factories.dpf_adat,
    da_product_factories.t_stamp
   FROM (public.da_product_factories
     JOIN ( SELECT mbag_da_product_view.dp_product_no,
            mbag_da_product_view.dp_structuring_type,
            mbag_da_product_view.dp_title,
            mbag_da_product_view.dp_picture,
            mbag_da_product_view.dp_product_grp,
            mbag_da_product_view.dp_aggregate_type,
            mbag_da_product_view.dp_assortment_classes,
            mbag_da_product_view.dp_docu_method,
            mbag_da_product_view.dp_product_visible,
            mbag_da_product_view.dp_kz_delta,
            mbag_da_product_view.dp_migration,
            mbag_da_product_view.dp_migration_date,
            mbag_da_product_view.dp_dataset_date,
            mbag_da_product_view.dp_source,
            mbag_da_product_view.dp_asproduct_classes,
            mbag_da_product_view.dp_comment,
            mbag_da_product_view.dp_series_ref,
            mbag_da_product_view.dp_is_special_cat,
            mbag_da_product_view.dp_aps_remark,
            mbag_da_product_view.dp_aps_code,
            mbag_da_product_view.dp_aps_from_idents,
            mbag_da_product_view.dp_aps_to_idents,
            mbag_da_product_view.dp_ident_class_old,
            mbag_da_product_view.dp_epc_relevant,
            mbag_da_product_view.dp_valid_countries,
            mbag_da_product_view.dp_invalid_countries,
            mbag_da_product_view.dp_brand,
            mbag_da_product_view.dp_second_parts_enabled,
            mbag_da_product_view.dp_ttz_filter,
            mbag_da_product_view.dp_scoring_with_mcodes,
            mbag_da_product_view.dp_disabled_filters,
            mbag_da_product_view.dp_modification_timestamp,
            mbag_da_product_view.dp_show_sas,
            mbag_da_product_view.dp_cab_fallback,
            mbag_da_product_view.t_stamp,
            mbag_da_product_view.dp_no_primus_hints,
            mbag_da_product_view.dp_psk,
            mbag_da_product_view.dp_use_svgs,
            mbag_da_product_view.dp_prefer_svg,
            mbag_da_product_view.dp_ident_factory_filtering,
            mbag_da_product_view.dp_full_language_support,
            mbag_da_product_view.dp_es_export_timestamp,
            mbag_da_product_view.dp_dialog_pos_check,
            mbag_da_product_view.dp_supplier_no,
            mbag_da_product_view.dp_car_perspective,
            mbag_da_product_view.dp_use_factory,
            mbag_da_product_view.dp_connect_data_visible,
            mbag_da_product_view.dp_fins
           FROM public.mbag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_product_factories.dpf_product_no)::text)));


--
-- Name: mbag_da_product_factories_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_product_factories_view AS
 SELECT da_product_factories.dpf_product_no,
    da_product_factories.dpf_factory_no,
    da_product_factories.dpf_edat,
    da_product_factories.dpf_adat,
    da_product_factories.t_stamp
   FROM (public.da_product_factories
     JOIN ( SELECT mbag_da_product_view.dp_product_no,
            mbag_da_product_view.dp_structuring_type,
            mbag_da_product_view.dp_title,
            mbag_da_product_view.dp_picture,
            mbag_da_product_view.dp_product_grp,
            mbag_da_product_view.dp_aggregate_type,
            mbag_da_product_view.dp_assortment_classes,
            mbag_da_product_view.dp_docu_method,
            mbag_da_product_view.dp_product_visible,
            mbag_da_product_view.dp_kz_delta,
            mbag_da_product_view.dp_migration,
            mbag_da_product_view.dp_migration_date,
            mbag_da_product_view.dp_dataset_date,
            mbag_da_product_view.dp_source,
            mbag_da_product_view.dp_asproduct_classes,
            mbag_da_product_view.dp_comment,
            mbag_da_product_view.dp_series_ref,
            mbag_da_product_view.dp_is_special_cat,
            mbag_da_product_view.dp_aps_remark,
            mbag_da_product_view.dp_aps_code,
            mbag_da_product_view.dp_aps_from_idents,
            mbag_da_product_view.dp_aps_to_idents,
            mbag_da_product_view.dp_ident_class_old,
            mbag_da_product_view.dp_epc_relevant,
            mbag_da_product_view.dp_valid_countries,
            mbag_da_product_view.dp_invalid_countries,
            mbag_da_product_view.dp_brand,
            mbag_da_product_view.dp_second_parts_enabled,
            mbag_da_product_view.dp_ttz_filter,
            mbag_da_product_view.dp_scoring_with_mcodes,
            mbag_da_product_view.dp_disabled_filters,
            mbag_da_product_view.dp_modification_timestamp,
            mbag_da_product_view.dp_show_sas,
            mbag_da_product_view.dp_cab_fallback,
            mbag_da_product_view.t_stamp,
            mbag_da_product_view.dp_no_primus_hints,
            mbag_da_product_view.dp_psk,
            mbag_da_product_view.dp_use_svgs,
            mbag_da_product_view.dp_prefer_svg,
            mbag_da_product_view.dp_ident_factory_filtering,
            mbag_da_product_view.dp_full_language_support,
            mbag_da_product_view.dp_es_export_timestamp,
            mbag_da_product_view.dp_dialog_pos_check,
            mbag_da_product_view.dp_supplier_no,
            mbag_da_product_view.dp_car_perspective,
            mbag_da_product_view.dp_use_factory,
            mbag_da_product_view.dp_connect_data_visible,
            mbag_da_product_view.dp_fins
           FROM public.mbag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_product_factories.dpf_product_no)::text)));


--
-- Name: mbag_da_product_models_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_product_models_view AS
 SELECT da_product_models.dpm_product_no,
    da_product_models.dpm_model_no,
    da_product_models.dpm_steering,
    da_product_models.dpm_textnr,
    da_product_models.dpm_valid_from,
    da_product_models.dpm_valid_to,
    da_product_models.dpm_model_visible,
    da_product_models.t_stamp
   FROM (public.da_product_models
     JOIN public.mbag_da_product_view ON (((mbag_da_product_view.dp_product_no)::text = (da_product_models.dpm_product_no)::text)));


--
-- Name: mbag_da_product_modules_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_product_modules_view AS
 SELECT da_product_modules.dpm_product_no,
    da_product_modules.dpm_module_no,
    da_product_modules.t_stamp
   FROM public.da_product_modules
  WHERE ((da_product_modules.dpm_product_no)::text IN ( SELECT mbag_da_product_view.dp_product_no
           FROM public.mbag_da_product_view));


--
-- Name: mbag_da_product_sas_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_product_sas_view AS
 SELECT da_product_sas.dps_product_no,
    da_product_sas.dps_sa_no,
    da_product_sas.dps_kg,
    da_product_sas.t_stamp,
    da_product_sas.dps_source
   FROM public.da_product_sas
  WHERE ((da_product_sas.dps_product_no)::text IN ( SELECT mbag_da_product_view.dp_product_no
           FROM public.mbag_da_product_view));


--
-- Name: mbag_da_replace_part_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_replace_part_view AS
 SELECT da_replace_part.drp_vari,
    da_replace_part.drp_ver,
    da_replace_part.drp_lfdnr,
    da_replace_part.drp_seqno,
    da_replace_part.drp_replace_matnr,
    da_replace_part.drp_replace_lfdnr,
    da_replace_part.drp_replace_rfmea,
    da_replace_part.drp_replace_rfmen,
    da_replace_part.t_stamp,
    da_replace_part.drp_source,
    da_replace_part.drp_status,
    da_replace_part.drp_source_guid,
    da_replace_part.drp_replace_source_guid
   FROM public.da_replace_part
  WHERE ((da_replace_part.drp_vari)::text IN ( SELECT da_module.dm_module_no
           FROM public.da_module
          WHERE ((da_module.dm_module_no)::text IN ( SELECT mbag_da_module_view.dm_module_no
                   FROM public.mbag_da_module_view))));


--
-- Name: mbag_da_report_const_nodes_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_report_const_nodes_view AS
 SELECT da_report_const_nodes.drcn_series_no,
    da_report_const_nodes.drcn_node_id,
    da_report_const_nodes.drcn_changeset_guid,
    da_report_const_nodes.drcn_open_entries,
    da_report_const_nodes.drcn_changed_entries,
    da_report_const_nodes.drcn_calculation_date,
    da_report_const_nodes.t_stamp
   FROM public.da_report_const_nodes;


--
-- Name: mbag_da_response_data_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_response_data_view AS
 SELECT da_response_data.drd_factory,
    da_response_data.drd_series_no,
    da_response_data.drd_aa,
    da_response_data.drd_bmaa,
    da_response_data.drd_pem,
    da_response_data.drd_adat,
    da_response_data.drd_ident,
    da_response_data.drd_as_data,
    da_response_data.drd_steering,
    da_response_data.drd_text,
    da_response_data.drd_agg_type,
    da_response_data.drd_valid,
    da_response_data.drd_source,
    da_response_data.drd_whc,
    da_response_data.drd_type,
    da_response_data.drd_status,
    da_response_data.t_stamp
   FROM public.da_response_data;


--
-- Name: mbag_da_response_spikes_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_response_spikes_view AS
 SELECT da_response_spikes.drs_factory,
    da_response_spikes.drs_series_no,
    da_response_spikes.drs_aa,
    da_response_spikes.drs_bmaa,
    da_response_spikes.drs_ident,
    da_response_spikes.drs_spike_ident,
    da_response_spikes.drs_pem,
    da_response_spikes.drs_adat,
    da_response_spikes.drs_as_data,
    da_response_spikes.drs_steering,
    da_response_spikes.drs_valid,
    da_response_spikes.drs_source,
    da_response_spikes.drs_status,
    da_response_spikes.t_stamp
   FROM public.da_response_spikes;


--
-- Name: mbag_da_sa_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_sa_view AS
 SELECT da_sa.ds_sa,
    da_sa.ds_desc,
    da_sa.ds_edat,
    da_sa.ds_adat,
    da_sa.ds_codes,
    da_sa.ds_source,
    da_sa.t_stamp,
    da_sa.ds_not_docu_relevant,
    da_sa.ds_const_desc,
    da_sa.ds_const_sa
   FROM public.da_sa
  WHERE ((da_sa.ds_sa)::text IN ( SELECT substr((dwarray.dwa_token)::text, 1, (length((dwarray.dwa_token)::text) - 2)) AS sa
           FROM public.dwarray
          WHERE (((dwarray.dwa_feld)::text = 'KATALOG.K_SA_VALIDITY'::text) AND ((dwarray.dwa_arrayid)::text IN ( SELECT katalog.k_sa_validity
                   FROM public.katalog
                  WHERE ((katalog.k_vari)::text IN ( SELECT mbag_da_module_view.dm_module_no
                           FROM public.mbag_da_module_view)))))));


--
-- Name: mbag_da_saa_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_saa_view AS
 SELECT da_saa.ds_saa,
    da_saa.ds_desc,
    da_saa.ds_const_desc,
    da_saa.ds_desc_extended,
    da_saa.ds_remark,
    da_saa.ds_rev_from,
    da_saa.ds_edat,
    da_saa.ds_adat,
    da_saa.ds_connected_sas,
    da_saa.ds_source,
    da_saa.t_stamp,
    da_saa.ds_kg,
    da_saa.ds_saa_ref,
    da_saa.ds_const_saa,
    dwa.dwa_token
   FROM (public.da_saa
     LEFT JOIN ( SELECT dwarray.dwa_token
           FROM public.dwarray
          WHERE (((dwarray.dwa_feld)::text = 'KATALOG.K_SA_VALIDITY'::text) AND ((dwarray.dwa_arrayid)::text IN ( SELECT katalog.k_sa_validity
                   FROM public.katalog
                  WHERE ((katalog.k_vari)::text IN ( SELECT mbag_da_module_view.dm_module_no
                           FROM public.mbag_da_module_view)))))) dwa ON (((da_saa.ds_saa)::text = (dwa.dwa_token)::text)));


--
-- Name: mbag_da_series_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_series_view AS
 SELECT da_series.ds_series_no,
    da_series.ds_type,
    da_series.ds_name,
    da_series.ds_data,
    da_series.ds_datb,
    da_series.ds_product_grp,
    da_series.ds_component_flag,
    da_series.ds_spare_part,
    da_series.ds_import_relevant,
    da_series.ds_event_flag,
    da_series.ds_alternative_calc,
    da_series.ds_hierarchy,
    da_series.t_stamp,
    da_series.ds_merge_products,
    da_series.ds_auto_calculation,
    da_series.ds_aa_wo_factory_data,
    da_series.ds_v_position_check
   FROM public.da_series;


--
-- Name: mbag_da_top_tus_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_top_tus_view AS
 SELECT da_top_tus.dtt_product_no,
    da_top_tus.dtt_country_code,
    da_top_tus.dtt_kg,
    da_top_tus.dtt_tu,
    da_top_tus.dtt_rank,
    da_top_tus.t_stamp
   FROM (public.da_top_tus
     JOIN ( SELECT mbag_da_product_view.dp_product_no,
            mbag_da_product_view.dp_structuring_type,
            mbag_da_product_view.dp_title,
            mbag_da_product_view.dp_picture,
            mbag_da_product_view.dp_product_grp,
            mbag_da_product_view.dp_aggregate_type,
            mbag_da_product_view.dp_assortment_classes,
            mbag_da_product_view.dp_docu_method,
            mbag_da_product_view.dp_product_visible,
            mbag_da_product_view.dp_kz_delta,
            mbag_da_product_view.dp_migration,
            mbag_da_product_view.dp_migration_date,
            mbag_da_product_view.dp_dataset_date,
            mbag_da_product_view.dp_source,
            mbag_da_product_view.dp_asproduct_classes,
            mbag_da_product_view.dp_comment,
            mbag_da_product_view.dp_series_ref,
            mbag_da_product_view.dp_is_special_cat,
            mbag_da_product_view.dp_aps_remark,
            mbag_da_product_view.dp_aps_code,
            mbag_da_product_view.dp_aps_from_idents,
            mbag_da_product_view.dp_aps_to_idents,
            mbag_da_product_view.dp_ident_class_old,
            mbag_da_product_view.dp_epc_relevant,
            mbag_da_product_view.dp_valid_countries,
            mbag_da_product_view.dp_invalid_countries,
            mbag_da_product_view.dp_brand,
            mbag_da_product_view.dp_second_parts_enabled,
            mbag_da_product_view.dp_ttz_filter,
            mbag_da_product_view.dp_scoring_with_mcodes,
            mbag_da_product_view.dp_disabled_filters,
            mbag_da_product_view.dp_modification_timestamp,
            mbag_da_product_view.dp_show_sas,
            mbag_da_product_view.dp_cab_fallback,
            mbag_da_product_view.t_stamp,
            mbag_da_product_view.dp_no_primus_hints,
            mbag_da_product_view.dp_psk,
            mbag_da_product_view.dp_use_svgs,
            mbag_da_product_view.dp_prefer_svg,
            mbag_da_product_view.dp_ident_factory_filtering,
            mbag_da_product_view.dp_full_language_support,
            mbag_da_product_view.dp_es_export_timestamp,
            mbag_da_product_view.dp_dialog_pos_check,
            mbag_da_product_view.dp_supplier_no,
            mbag_da_product_view.dp_car_perspective,
            mbag_da_product_view.dp_use_factory,
            mbag_da_product_view.dp_connect_data_visible,
            mbag_da_product_view.dp_fins
           FROM public.mbag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_top_tus.dtt_product_no)::text)));


--
-- Name: mbag_da_vin_model_mapping_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_vin_model_mapping_view AS
 SELECT da_vin_model_mapping.dvm_vin_prefix,
    da_vin_model_mapping.dvm_model_prefix,
    da_vin_model_mapping.t_stamp
   FROM public.da_vin_model_mapping
  WHERE ((da_vin_model_mapping.dvm_model_prefix)::text IN ( SELECT substr((da_product_models.dpm_model_no)::text, 2, 4) AS substr
           FROM (public.da_product_models
             JOIN ( SELECT mbag_da_product_view.dp_product_no,
                    mbag_da_product_view.dp_structuring_type,
                    mbag_da_product_view.dp_title,
                    mbag_da_product_view.dp_picture,
                    mbag_da_product_view.dp_product_grp,
                    mbag_da_product_view.dp_aggregate_type,
                    mbag_da_product_view.dp_assortment_classes,
                    mbag_da_product_view.dp_docu_method,
                    mbag_da_product_view.dp_product_visible,
                    mbag_da_product_view.dp_kz_delta,
                    mbag_da_product_view.dp_migration,
                    mbag_da_product_view.dp_migration_date,
                    mbag_da_product_view.dp_dataset_date,
                    mbag_da_product_view.dp_source,
                    mbag_da_product_view.dp_asproduct_classes,
                    mbag_da_product_view.dp_comment,
                    mbag_da_product_view.dp_series_ref,
                    mbag_da_product_view.dp_is_special_cat,
                    mbag_da_product_view.dp_aps_remark,
                    mbag_da_product_view.dp_aps_code,
                    mbag_da_product_view.dp_aps_from_idents,
                    mbag_da_product_view.dp_aps_to_idents,
                    mbag_da_product_view.dp_ident_class_old,
                    mbag_da_product_view.dp_epc_relevant,
                    mbag_da_product_view.dp_valid_countries,
                    mbag_da_product_view.dp_invalid_countries,
                    mbag_da_product_view.dp_brand,
                    mbag_da_product_view.dp_second_parts_enabled,
                    mbag_da_product_view.dp_ttz_filter,
                    mbag_da_product_view.dp_scoring_with_mcodes,
                    mbag_da_product_view.dp_disabled_filters,
                    mbag_da_product_view.dp_modification_timestamp,
                    mbag_da_product_view.dp_show_sas,
                    mbag_da_product_view.dp_cab_fallback,
                    mbag_da_product_view.t_stamp,
                    mbag_da_product_view.dp_no_primus_hints,
                    mbag_da_product_view.dp_psk,
                    mbag_da_product_view.dp_use_svgs,
                    mbag_da_product_view.dp_prefer_svg,
                    mbag_da_product_view.dp_ident_factory_filtering,
                    mbag_da_product_view.dp_full_language_support,
                    mbag_da_product_view.dp_es_export_timestamp,
                    mbag_da_product_view.dp_dialog_pos_check,
                    mbag_da_product_view.dp_supplier_no,
                    mbag_da_product_view.dp_car_perspective,
                    mbag_da_product_view.dp_use_factory,
                    mbag_da_product_view.dp_connect_data_visible,
                    mbag_da_product_view.dp_fins
                   FROM public.mbag_da_product_view) da_product ON (((da_product.dp_product_no)::text = (da_product_models.dpm_product_no)::text)))));


--
-- Name: mbag_da_wire_harness_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_da_wire_harness_view AS
 SELECT da_wire_harness.dwh_snr,
    da_wire_harness.dwh_ref,
    da_wire_harness.dwh_connector_no,
    da_wire_harness.dwh_sub_snr,
    da_wire_harness.dwh_pos,
    da_wire_harness.dwh_snr_type,
    da_wire_harness.dwh_contact_dataset_date,
    da_wire_harness.dwh_part_dataset_date,
    da_wire_harness.t_stamp,
    da_wire_harness.dwh_contact_add_text
   FROM public.da_wire_harness;


--
-- Name: mbag_dwarray_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_dwarray_view AS
 SELECT dwarray.dwa_feld,
    dwarray.dwa_arrayid,
    dwarray.dwa_lfdnr,
    dwarray.dwa_token,
    dwarray.t_stamp
   FROM public.dwarray
  WHERE ((length((dwarray.dwa_arrayid)::text) >= 7) AND (substr((dwarray.dwa_arrayid)::text, 1, (length((dwarray.dwa_arrayid)::text) - 7)) IN ( SELECT mbag_da_module_view.dm_module_no
           FROM public.mbag_da_module_view)));


--
-- Name: mbag_images_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_images_view AS
 SELECT images.i_tiffname,
    images.i_ver,
    images.i_blatt,
    images.i_images,
    images.i_pver,
    images.t_stamp,
    images.i_katalog,
    images.i_imagedate,
    images.i_codes,
    images.i_model_validity,
    images.i_saa_constkit_validity,
    images.i_event_from,
    images.i_event_to,
    images.i_psk_variant_validity,
    images.i_only_fin_visible,
    images.i_navigation_perspective
   FROM public.images
  WHERE ((images.i_tiffname)::text IN ( SELECT mbag_da_module_view.dm_module_no
           FROM public.mbag_da_module_view));


--
-- Name: mbag_katalog_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_katalog_view AS
 SELECT katalog.k_vari,
    katalog.k_ver,
    katalog.k_lfdnr,
    katalog.k_sach,
    katalog.k_sver,
    katalog.k_art,
    katalog.k_pos,
    katalog.k_ebene,
    katalog.k_matnr,
    katalog.k_mver,
    katalog.k_menge,
    katalog.k_mengeart,
    katalog.k_bestflag,
    katalog.t_stamp,
    katalog.k_seqnr,
    katalog.k_bmk,
    katalog.k_codes,
    katalog.k_minusparts,
    katalog.k_hierarchy,
    katalog.k_steering,
    katalog.k_source_type,
    katalog.k_source_context,
    katalog.k_source_ref1,
    katalog.k_source_ref2,
    katalog.k_source_guid,
    katalog.k_product_grp,
    katalog.k_sa_validity,
    katalog.k_model_validity,
    katalog.k_aa,
    katalog.k_gearbox_type,
    katalog.k_etz,
    katalog.k_virtual_mat_type,
    katalog.k_ww,
    katalog.k_ww_extra_parts,
    katalog.k_fail_loclist,
    katalog.k_as_code,
    katalog.k_acc_code,
    katalog.k_pclasses_validity,
    katalog.k_eval_pem_from,
    katalog.k_eval_pem_to,
    katalog.k_codes_const,
    katalog.k_hierarchy_const,
    katalog.k_menge_const,
    katalog.k_omit,
    katalog.k_etkz,
    katalog.k_only_model_filter,
    katalog.k_event_from,
    katalog.k_event_to,
    katalog.k_event_from_const,
    katalog.k_event_to_const,
    katalog.k_codes_reduced,
    katalog.k_datefrom,
    katalog.k_dateto,
    katalog.k_min_kem_date_from,
    katalog.k_max_kem_date_to,
    katalog.k_use_primus_successor,
    katalog.k_copy_vari,
    katalog.k_copy_lfdnr,
    katalog.k_copy_date,
    katalog.k_psk_variant_validity,
    katalog.k_auto_created,
    katalog.k_entry_locked,
    katalog.k_country_validity,
    katalog.k_spec_validity,
    katalog.k_was_auto_created
   FROM public.katalog
  WHERE ((katalog.k_vari)::text IN ( SELECT mbag_da_module_view.dm_module_no
           FROM public.mbag_da_module_view));


--
-- Name: mbag_links_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_links_view AS
 SELECT links.l_images,
    links.l_ver,
    links.l_lfdnr,
    links.l_sprach,
    links.l_usage,
    links.l_koord_l,
    links.l_koord_o,
    links.l_koord_r,
    links.l_koord_u,
    links.l_art,
    links.l_text,
    links.l_textver,
    links.l_extinfo,
    links.t_stamp
   FROM public.links
  WHERE ((links.l_images)::text IN ( SELECT images.i_images
           FROM public.images
          WHERE ((images.i_tiffname)::text IN ( SELECT mbag_da_module_view.dm_module_no
                   FROM public.mbag_da_module_view))));


--
-- Name: mbag_mat_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_mat_view AS
 SELECT mat.m_matnr,
    mat.m_ver,
    mat.m_textnr,
    mat.m_bestnr,
    mat.m_bestflag,
    mat.m_status,
    mat.t_stamp,
    mat.m_armored_ind,
    mat.m_as_es_1,
    mat.m_as_es_2,
    mat.m_assemblysign,
    mat.m_base_matnr,
    mat.m_brand,
    mat.m_certrel,
    mat.m_change_desc,
    mat.m_china_ind,
    mat.m_const_desc,
    mat.m_docreq,
    mat.m_esd_ind,
    mat.m_imagedate,
    mat.m_imagestate,
    mat.m_is_deleted,
    mat.m_layout_flag,
    mat.m_materialfinitestate,
    mat.m_nato_no,
    mat.m_noteone,
    mat.m_notetwo,
    mat.m_quantunit,
    mat.m_refser,
    mat.m_relatedpic,
    mat.m_releasestate,
    mat.m_reman_ind,
    mat.m_securitysign,
    mat.m_securitysign_repair,
    mat.m_shelf_life,
    mat.m_source,
    mat.m_state,
    mat.m_svhc_ind,
    mat.m_theftrel,
    mat.m_theftrelinfo,
    mat.m_variant_sign,
    mat.m_vedocsign,
    mat.m_weightcalc,
    mat.m_weightprog,
    mat.m_weightreal,
    mat.m_addtext,
    mat.m_verksnr,
    mat.m_factory_ids,
    mat.m_last_modified,
    mat.m_market_etkz,
    mat.m_internal_text,
    mat.m_basket_sign,
    mat.m_etkz,
    mat.m_etkz_old,
    mat.m_assembly,
    mat.m_etkz_mbs,
    mat.m_addtext_edited,
    mat.m_etkz_ctt,
    mat.m_image_available,
    mat.m_psk_material,
    mat.m_psk_supplier_no,
    mat.m_psk_manufacturer_no,
    mat.m_psk_supplier_matnr,
    mat.m_psk_manufacturer_matnr,
    mat.m_psk_image_no_extern,
    mat.m_psk_remark,
    mat.m_weight,
    mat.m_length,
    mat.m_width,
    mat.m_height,
    mat.m_volume,
    mat.m_matnr_mbag,
    mat.m_matnr_dtag,
    mat.m_hazardous_goods_indicator,
    mat.m_partno_basic,
    mat.m_partno_shortblock,
    mat.m_partno_longblock,
    mat.m_partno_longblock_plus
   FROM public.mat
  WHERE (substr((mat.m_matnr)::text, 1, 10) IN ( SELECT substr((katalog.k_matnr)::text, 1, 10) AS substr
           FROM public.katalog
          WHERE ((katalog.k_vari)::text IN ( SELECT mbag_da_module_view.dm_module_no
                   FROM public.mbag_da_module_view))
        UNION
         SELECT substr((da_replace_part.drp_replace_matnr)::text, 1, 10) AS substr
           FROM public.da_replace_part
          WHERE ((da_replace_part.drp_vari)::text IN ( SELECT mbag_da_module_view.dm_module_no
                   FROM public.mbag_da_module_view))
        UNION
         SELECT substr((da_include_part.dip_include_matnr)::text, 1, 10) AS substr
           FROM public.da_include_part
          WHERE ((da_include_part.dip_vari)::text IN ( SELECT mbag_da_module_view.dm_module_no
                   FROM public.mbag_da_module_view))));


--
-- Name: mbag_pool_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_pool_view AS
 SELECT pool.p_images,
    pool.p_ver,
    pool.p_sprach,
    pool.p_usage,
    pool.p_data,
    pool.p_imgtype,
    pool.p_aspectratio,
    pool.t_stamp,
    pool.p_ratio,
    pool.p_isdraft,
    pool.p_lastdate,
    pool.p_importdate,
    pool.p_preview_data,
    pool.p_preview_imgtype,
    pool.p_validity_scope
   FROM public.pool
  WHERE ((pool.p_images)::text IN ( SELECT images.i_images
           FROM public.images
          WHERE ((images.i_tiffname)::text IN ( SELECT mbag_da_module_view.dm_module_no
                   FROM public.mbag_da_module_view))));


--
-- Name: mbag_poolentry_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_poolentry_view AS
 SELECT poolentry.pe_images,
    poolentry.pe_ver,
    poolentry.pe_bem,
    poolentry.t_stamp
   FROM public.poolentry
  WHERE ((poolentry.pe_images)::text IN ( SELECT images.i_images
           FROM public.images
          WHERE ((images.i_tiffname)::text IN ( SELECT mbag_da_module_view.dm_module_no
                   FROM public.mbag_da_module_view))));


--
-- Name: mbag_sprache_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.mbag_sprache_view AS
 SELECT sprache.s_feld,
    sprache.s_sprach,
    sprache.s_textnr,
    sprache.s_benenn,
    sprache.s_textid,
    sprache.s_benenn_lang,
    sprache.t_stamp
   FROM public.sprache;


--
-- Name: notiz; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notiz (
    n_userid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_sprach character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_typ character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_status character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_kvari character varying(200) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_kver character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_dver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_klfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_titel character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_text text COLLATE pg_catalog."en-US-x-icu",
    n_category character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_attach bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_xml character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_public character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: preise; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.preise (
    p_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_mver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_wkz character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_preis character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_idkey character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    p_eorderno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: s_chain; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.s_chain (
    sc_vari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    sc_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    sc_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    sc_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    sc_mver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    sc_st_source character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    sc_st_source_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    sc_backflag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    sc_st_dest character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    sc_st_dest_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    sc_st_dest_sort character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: s_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.s_items (
    si_st_nr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    si_st_ver character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    si_lfdnr character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    si_matnr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    si_mver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    si_menge character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    si_mengeart character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    si_seqnr character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_addtext character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_addtext2 character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_constructionkits text COLLATE pg_catalog."en-US-x-icu",
    s_addpart character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: s_set; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.s_set (
    st_st_nr character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    st_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    st_bestflag character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_title character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_ismodul character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: sbadr; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sbadr (
    a_id character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_name character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_name2 character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_contact character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_street character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_zip character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_city character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_country character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_phone character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_fax character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_email character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_url character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_adressid character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: sbdetail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sbdetail (
    b_satzid character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_user character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_key character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    b_value character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: strukt; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.strukt (
    s_knoten character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_lfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_vknoten character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_vver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_text character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_typ character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_icon text COLLATE pg_catalog."en-US-x-icu",
    s_kvari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_kver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_seqnr character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_klfdnr character varying(20) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    s_statinprogress character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: treeid; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.treeid (
    t_id integer DEFAULT 0 NOT NULL,
    t_vari character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_ver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: treemod; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.treemod (
    t_parent integer DEFAULT 0 NOT NULL,
    t_child integer DEFAULT 0 NOT NULL
);


--
-- Name: u_serno; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.u_serno (
    u_serno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_modno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_modver character varying(5) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_type character varying(3) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_data bytea,
    u_bdate character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_orderno character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_vin character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_apps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_apps (
    a_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    a_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_news; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_news (
    n_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_publisher_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_publisher_org_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_publisher_app_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_publisher_scope_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_publisher_scope_constraints bytea,
    n_no_read_unread_distinction character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_reader_feedback_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_style_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_archived character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_sent_per_email character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_sent_per_email_only character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_tags character varying(2000) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    n_creation_ts character varying(28) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_news_feedback; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_news_feedback (
    nf_news_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    nf_user_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    nf_read character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    nf_read_apps character varying(1000) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_news_texts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_news_texts (
    nt_news_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    nt_language character varying(2) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    nt_content bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_organisation_apps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_organisation_apps (
    oa_organisation_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    oa_app_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_organisation_properties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_organisation_properties (
    op_organisation_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    op_app_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    op_key character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    op_type character varying(30) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    op_inheritance character varying(30) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    op_value character varying(4000) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    op_blob bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_organisation_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_organisation_roles (
    or_organisation_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    or_role_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_organisations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_organisations (
    o_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    o_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    o_parent_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    o_ext_id character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_rights; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_rights (
    r_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    r_app_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    r_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    r_hasscope character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_role_rights; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_role_rights (
    rr_role_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    rr_right_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    rr_scope character varying(30) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_roles (
    r_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    r_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    r_global character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    r_scope character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_user_admin_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_user_admin_history (
    uah_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    uah_timestamp character varying(13) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    uah_user_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    uah_editing_user_info character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    uah_action character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    uah_old_value text COLLATE pg_catalog."en-US-x-icu",
    uah_new_value text COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_user_data_templates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_user_data_templates (
    udt_key character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    udt_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    udt_type character varying(30) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_user_organisations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_user_organisations (
    uo_user_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    uo_organisation_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_user_prop_templates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_user_prop_templates (
    upt_app_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    upt_key character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    upt_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    upt_type character varying(30) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    upt_value character varying(4000) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    upt_changelevel character varying(10) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    upt_blob bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    upt_showreadonly character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_user_properties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_user_properties (
    up_user_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    up_app_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    up_org_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    up_key character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    up_type character varying(30) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    up_value character varying(4000) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    up_blob bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_user_roles (
    ur_user_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    ur_role_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: ua_users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ua_users (
    u_id character varying(50) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_name character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_password character varying(250) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_active character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_salt character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_password2 character varying(100) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: usergroup; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usergroup (
    u_ugid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_type character varying(1) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_name character varying(300) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    u_inactive character varying(1) DEFAULT '0'::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: usersettings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usersettings (
    us_userid character varying(38) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu",
    us_settings bytea,
    t_stamp character varying(8) DEFAULT ''::character varying NOT NULL COLLATE pg_catalog."en-US-x-icu"
);


--
-- Name: bestell bestell_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bestell
    ADD CONSTRAINT bestell_pkey PRIMARY KEY (b_user, b_satzid, b_lfdnr);


--
-- Name: custprop custprop_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custprop
    ADD CONSTRAINT custprop_pkey PRIMARY KEY (c_kvari, c_kver, c_klfdnr, c_matnr, c_mver, c_key, c_sprach);


--
-- Name: da_ac_pc_mapping da_ac_pc_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_ac_pc_mapping
    ADD CONSTRAINT da_ac_pc_mapping_pkey PRIMARY KEY (dapm_assortment_class);


--
-- Name: da_ac_pc_permission_mapping da_ac_pc_permission_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_ac_pc_permission_mapping
    ADD CONSTRAINT da_ac_pc_permission_mapping_pkey PRIMARY KEY (dppm_brand, dppm_assortment_class, dppm_as_product_class);


--
-- Name: da_acc_codes da_acc_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_acc_codes
    ADD CONSTRAINT da_acc_codes_pkey PRIMARY KEY (dacc_code);


--
-- Name: da_agg_part_codes da_agg_part_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_agg_part_codes
    ADD CONSTRAINT da_agg_part_codes_pkey PRIMARY KEY (dapc_part_no, dapc_code, dapc_series_no, dapc_factory, dapc_factory_sign, dapc_date_from, dapc_date_to);


--
-- Name: da_aggs_mapping da_aggs_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_aggs_mapping
    ADD CONSTRAINT da_aggs_mapping_pkey PRIMARY KEY (dam_dialog_agg_type);


--
-- Name: da_ao_history da_ao_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_ao_history
    ADD CONSTRAINT da_ao_history_pkey PRIMARY KEY (dah_guid, dah_seqno);


--
-- Name: da_as_codes da_as_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_as_codes
    ADD CONSTRAINT da_as_codes_pkey PRIMARY KEY (das_code);


--
-- Name: da_author_order da_author_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_author_order
    ADD CONSTRAINT da_author_order_pkey PRIMARY KEY (dao_guid);


--
-- Name: da_bad_code da_bad_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_bad_code
    ADD CONSTRAINT da_bad_code_pkey PRIMARY KEY (dbc_series_no, dbc_aa, dbc_code_id);


--
-- Name: da_bom_mat_history da_bom_mat_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_bom_mat_history
    ADD CONSTRAINT da_bom_mat_history_pkey PRIMARY KEY (dbmh_part_no, dbmh_part_ver, dbmh_rev_from);


--
-- Name: da_branch_pc_mapping da_branch_pc_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_branch_pc_mapping
    ADD CONSTRAINT da_branch_pc_mapping_pkey PRIMARY KEY (dbm_branch);


--
-- Name: da_change_set_entry da_change_set_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_change_set_entry
    ADD CONSTRAINT da_change_set_entry_pkey PRIMARY KEY (dce_guid, dce_do_type, dce_do_id);


--
-- Name: da_change_set_info_defs da_change_set_info_defs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_change_set_info_defs
    ADD CONSTRAINT da_change_set_info_defs_pkey PRIMARY KEY (dcid_do_type, dcid_feld, dcid_as_relevant);


--
-- Name: da_change_set da_change_set_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_change_set
    ADD CONSTRAINT da_change_set_pkey PRIMARY KEY (dcs_guid);


--
-- Name: da_code_mapping da_code_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_code_mapping
    ADD CONSTRAINT da_code_mapping_pkey PRIMARY KEY (dcm_category, dcm_model_type_id, dcm_initial_code, dcm_target_code);


--
-- Name: da_code da_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_code
    ADD CONSTRAINT da_code_pkey PRIMARY KEY (dc_code_id, dc_series_no, dc_pgrp, dc_sdata, dc_source);


--
-- Name: da_color_number da_color_number_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_color_number
    ADD CONSTRAINT da_color_number_pkey PRIMARY KEY (dcn_color_no);


--
-- Name: da_colortable_content da_colortable_content_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_colortable_content
    ADD CONSTRAINT da_colortable_content_pkey PRIMARY KEY (dctc_table_id, dctc_pos, dctc_sdata);


--
-- Name: da_colortable_data da_colortable_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_colortable_data
    ADD CONSTRAINT da_colortable_data_pkey PRIMARY KEY (dctd_table_id);


--
-- Name: da_colortable_factory da_colortable_factory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_colortable_factory
    ADD CONSTRAINT da_colortable_factory_pkey PRIMARY KEY (dccf_table_id, dccf_pos, dccf_factory, dccf_adat, dccf_data_id, dccf_sdata);


--
-- Name: da_colortable_part da_colortable_part_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_colortable_part
    ADD CONSTRAINT da_colortable_part_pkey PRIMARY KEY (dctp_table_id, dctp_pos, dctp_sdata);


--
-- Name: da_comb_text da_comb_text_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_comb_text
    ADD CONSTRAINT da_comb_text_pkey PRIMARY KEY (dct_module, dct_modver, dct_seqno, dct_text_seqno);


--
-- Name: da_confirm_changes da_confirm_changes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_confirm_changes
    ADD CONSTRAINT da_confirm_changes_pkey PRIMARY KEY (dcc_change_set_id, dcc_do_type, dcc_do_id, dcc_partlist_entry_id);


--
-- Name: da_const_kit_content da_const_kit_content_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_const_kit_content
    ADD CONSTRAINT da_const_kit_content_pkey PRIMARY KEY (dckc_part_no, dckc_pose, dckc_ww, dckc_sda);


--
-- Name: da_const_status_codes da_const_status_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_const_status_codes
    ADD CONSTRAINT da_const_status_codes_pkey PRIMARY KEY (dasc_code);


--
-- Name: da_cortex_import_data da_cortex_import_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_cortex_import_data
    ADD CONSTRAINT da_cortex_import_data_pkey PRIMARY KEY (dci_creation_ts, dci_endpoint_name);


--
-- Name: da_country_code_mapping da_country_code_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_country_code_mapping
    ADD CONSTRAINT da_country_code_mapping_pkey PRIMARY KEY (dcm_region_code);


--
-- Name: da_country_invalid_parts da_country_invalid_parts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_country_invalid_parts
    ADD CONSTRAINT da_country_invalid_parts_pkey PRIMARY KEY (dcip_part_no, dcip_country_code);


--
-- Name: da_country_valid_series da_country_valid_series_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_country_valid_series
    ADD CONSTRAINT da_country_valid_series_pkey PRIMARY KEY (dcvs_series_no, dcvs_country_code);


--
-- Name: da_dialog_add_data da_dialog_add_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dialog_add_data
    ADD CONSTRAINT da_dialog_add_data_pkey PRIMARY KEY (dad_guid, dad_adat);


--
-- Name: da_dialog_changes da_dialog_changes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dialog_changes
    ADD CONSTRAINT da_dialog_changes_pkey PRIMARY KEY (ddc_do_type, ddc_do_id, ddc_hash);


--
-- Name: da_dialog_dsr da_dialog_dsr_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dialog_dsr
    ADD CONSTRAINT da_dialog_dsr_pkey PRIMARY KEY (dsr_matnr, dsr_type, dsr_no, dsr_sdata, dsr_mk4, dsr_mk5);


--
-- Name: da_dialog_partlist_text da_dialog_partlist_text_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dialog_partlist_text
    ADD CONSTRAINT da_dialog_partlist_text_pkey PRIMARY KEY (dd_plt_br, dd_plt_hm, dd_plt_m, dd_plt_sm, dd_plt_pose, dd_plt_posv, dd_plt_ww, dd_plt_etz, dd_plt_textkind, dd_plt_sdata);


--
-- Name: da_dialog da_dialog_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dialog
    ADD CONSTRAINT da_dialog_pkey PRIMARY KEY (dd_guid);


--
-- Name: da_dialog_pos_text da_dialog_pos_text_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dialog_pos_text
    ADD CONSTRAINT da_dialog_pos_text_pkey PRIMARY KEY (dd_pos_br, dd_pos_hm, dd_pos_m, dd_pos_sm, dd_pos_pos, dd_pos_sdata);


--
-- Name: da_dict_meta da_dict_meta_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dict_meta
    ADD CONSTRAINT da_dict_meta_pkey PRIMARY KEY (da_dict_meta_txtkind_id, da_dict_meta_textid);


--
-- Name: da_dict_sprache da_dict_sprache_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dict_sprache
    ADD CONSTRAINT da_dict_sprache_pkey PRIMARY KEY (da_dict_sprache_textid, da_dict_sprache_sprach);


--
-- Name: da_dict_trans_job_history da_dict_trans_job_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dict_trans_job_history
    ADD CONSTRAINT da_dict_trans_job_history_pkey PRIMARY KEY (dtjh_textid, dtjh_source_lang, dtjh_dest_lang, dtjh_jobid, dtjh_last_modified);


--
-- Name: da_dict_trans_job da_dict_trans_job_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dict_trans_job
    ADD CONSTRAINT da_dict_trans_job_pkey PRIMARY KEY (dtj_textid, dtj_source_lang, dtj_dest_lang, dtj_jobid);


--
-- Name: da_dict_txtkind da_dict_txtkind_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dict_txtkind
    ADD CONSTRAINT da_dict_txtkind_pkey PRIMARY KEY (da_dict_tk_txtkind_id);


--
-- Name: da_dict_txtkind_usage da_dict_txtkind_usage_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_dict_txtkind_usage
    ADD CONSTRAINT da_dict_txtkind_usage_pkey PRIMARY KEY (da_dict_tku_txtkind_id, da_dict_tku_feld);


--
-- Name: da_eds_const_kit da_eds_const_kit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_eds_const_kit
    ADD CONSTRAINT da_eds_const_kit_pkey PRIMARY KEY (dck_snr, dck_partpos, dck_revfrom);


--
-- Name: da_eds_const_props da_eds_const_props_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_eds_const_props
    ADD CONSTRAINT da_eds_const_props_pkey PRIMARY KEY (dcp_snr, dcp_partpos, dcp_btx_flag, dcp_revfrom);


--
-- Name: da_eds_mat_remarks da_eds_mat_remarks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_eds_mat_remarks
    ADD CONSTRAINT da_eds_mat_remarks_pkey PRIMARY KEY (demr_part_no, demr_rev_from, demr_remark_no);


--
-- Name: da_eds_mat_ww_flags da_eds_mat_ww_flags_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_eds_mat_ww_flags
    ADD CONSTRAINT da_eds_mat_ww_flags_pkey PRIMARY KEY (demw_part_no, demw_rev_from, demw_flag);


--
-- Name: da_eds_model da_eds_model_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_eds_model
    ADD CONSTRAINT da_eds_model_pkey PRIMARY KEY (eds_modelno, eds_group, eds_scope, eds_pos, eds_steering, eds_aa, eds_revfrom);


--
-- Name: da_eds_saa_models da_eds_saa_models_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_eds_saa_models
    ADD CONSTRAINT da_eds_saa_models_pkey PRIMARY KEY (da_esm_saa_no, da_esm_model_no);


--
-- Name: da_eds_saa_remarks da_eds_saa_remarks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_eds_saa_remarks
    ADD CONSTRAINT da_eds_saa_remarks_pkey PRIMARY KEY (desr_saa, desr_rev_from, desr_remark_no);


--
-- Name: da_eds_saa_ww_flags da_eds_saa_ww_flags_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_eds_saa_ww_flags
    ADD CONSTRAINT da_eds_saa_ww_flags_pkey PRIMARY KEY (desw_saa, desw_rev_from, desw_flag);


--
-- Name: da_einpas da_einpas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_einpas
    ADD CONSTRAINT da_einpas_pkey PRIMARY KEY (ep_hg, ep_g, ep_tu);


--
-- Name: da_einpasdsc da_einpasdsc_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_einpasdsc
    ADD CONSTRAINT da_einpasdsc_pkey PRIMARY KEY (ep_hg, ep_g, ep_tu);


--
-- Name: da_einpashmmsm da_einpashmmsm_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_einpashmmsm
    ADD CONSTRAINT da_einpashmmsm_pkey PRIMARY KEY (ep_series, ep_hm, ep_m, ep_sm, ep_lfdnr);


--
-- Name: da_einpaskgtu da_einpaskgtu_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_einpaskgtu
    ADD CONSTRAINT da_einpaskgtu_pkey PRIMARY KEY (ep_modeltype, ep_kg, ep_tu, ep_lfdnr);


--
-- Name: da_einpasops da_einpasops_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_einpasops
    ADD CONSTRAINT da_einpasops_pkey PRIMARY KEY (ep_group, ep_scope, ep_saaprefix, ep_lfdnr);


--
-- Name: da_epc_fn_content da_epc_fn_content_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_epc_fn_content
    ADD CONSTRAINT da_epc_fn_content_pkey PRIMARY KEY (defc_type, defc_text_id, defc_line_no);


--
-- Name: da_epc_fn_katalog_ref da_epc_fn_katalog_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_epc_fn_katalog_ref
    ADD CONSTRAINT da_epc_fn_katalog_ref_pkey PRIMARY KEY (defr_product_no, defr_kg, defr_fn_no, defr_text_id);


--
-- Name: da_epc_fn_sa_ref da_epc_fn_sa_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_epc_fn_sa_ref
    ADD CONSTRAINT da_epc_fn_sa_ref_pkey PRIMARY KEY (defs_sa_no, defs_fn_no);


--
-- Name: da_error_location da_error_location_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_error_location
    ADD CONSTRAINT da_error_location_pkey PRIMARY KEY (del_series_no, del_hm, del_m, del_sm, del_pose, del_partno, del_damage_part, del_sda);


--
-- Name: da_es1 da_es1_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_es1
    ADD CONSTRAINT da_es1_pkey PRIMARY KEY (des_es1, des_fnid);


--
-- Name: da_export_content da_export_content_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_export_content
    ADD CONSTRAINT da_export_content_pkey PRIMARY KEY (dec_job_id, dec_do_type, dec_do_id, dec_product_no);


--
-- Name: da_export_request da_export_request_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_export_request
    ADD CONSTRAINT da_export_request_pkey PRIMARY KEY (der_job_id);


--
-- Name: da_factories da_factories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_factories
    ADD CONSTRAINT da_factories_pkey PRIMARY KEY (df_letter_code);


--
-- Name: da_factory_data da_factory_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_factory_data
    ADD CONSTRAINT da_factory_data_pkey PRIMARY KEY (dfd_guid, dfd_factory, dfd_spkz, dfd_adat, dfd_data_id, dfd_seq_no);


--
-- Name: da_factory_model da_factory_model_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_factory_model
    ADD CONSTRAINT da_factory_model_pkey PRIMARY KEY (dfm_wmi, dfm_factory_sign, dfm_factory, dfm_model_prefix, dfm_add_factory, dfm_agg_type, dfm_belt_sign, dfm_belt_grouping);


--
-- Name: da_fn_content da_fn_content_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_fn_content
    ADD CONSTRAINT da_fn_content_pkey PRIMARY KEY (dfnc_fnid, dfnc_line_no);


--
-- Name: da_fn_katalog_ref da_fn_katalog_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_fn_katalog_ref
    ADD CONSTRAINT da_fn_katalog_ref_pkey PRIMARY KEY (dfnk_module, dfnk_modver, dfnk_seqno, dfnk_fnid);


--
-- Name: da_fn_mat_ref da_fn_mat_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_fn_mat_ref
    ADD CONSTRAINT da_fn_mat_ref_pkey PRIMARY KEY (dfnm_matnr, dfnm_fnid);


--
-- Name: da_fn da_fn_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_fn
    ADD CONSTRAINT da_fn_pkey PRIMARY KEY (dfn_id);


--
-- Name: da_fn_pos da_fn_pos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_fn_pos
    ADD CONSTRAINT da_fn_pos_pkey PRIMARY KEY (dfnp_guid, dfnp_sesi, dfnp_posp, dfnp_fn_no);


--
-- Name: da_fn_saa_ref da_fn_saa_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_fn_saa_ref
    ADD CONSTRAINT da_fn_saa_ref_pkey PRIMARY KEY (dfns_saa, dfns_fnid);


--
-- Name: da_generic_install_location da_generic_install_location_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_generic_install_location
    ADD CONSTRAINT da_generic_install_location_pkey PRIMARY KEY (dgil_series, dgil_hm, dgil_m, dgil_sm, dgil_pose, dgil_sda);


--
-- Name: da_generic_part da_generic_part_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_generic_part
    ADD CONSTRAINT da_generic_part_pkey PRIMARY KEY (dgp_guid);


--
-- Name: da_genvo_pairing da_genvo_pairing_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_genvo_pairing
    ADD CONSTRAINT da_genvo_pairing_pkey PRIMARY KEY (dgp_genvo_l, dgp_genvo_r);


--
-- Name: da_genvo_supp_text da_genvo_supp_text_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_genvo_supp_text
    ADD CONSTRAINT da_genvo_supp_text_pkey PRIMARY KEY (da_genvo_no);


--
-- Name: da_hmmsm_kgtu da_hmmsm_kgtu_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_hmmsm_kgtu
    ADD CONSTRAINT da_hmmsm_kgtu_pkey PRIMARY KEY (dhk_bcte);


--
-- Name: da_hmmsm da_hmmsm_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_hmmsm
    ADD CONSTRAINT da_hmmsm_pkey PRIMARY KEY (dh_series_no, dh_hm, dh_m, dh_sm);


--
-- Name: da_hmmsmdesc da_hmmsmdesc_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_hmmsmdesc
    ADD CONSTRAINT da_hmmsmdesc_pkey PRIMARY KEY (dh_series_no, dh_hm, dh_m, dh_sm);


--
-- Name: da_hmo_saa_mapping da_hmo_saa_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_hmo_saa_mapping
    ADD CONSTRAINT da_hmo_saa_mapping_pkey PRIMARY KEY (dhsm_hmo);


--
-- Name: da_include_const_mat da_include_const_mat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_include_const_mat
    ADD CONSTRAINT da_include_const_mat_pkey PRIMARY KEY (dicm_part_no, dicm_sdata, dicm_include_part_no);


--
-- Name: da_include_part da_include_part_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_include_part
    ADD CONSTRAINT da_include_part_pkey PRIMARY KEY (dip_vari, dip_ver, dip_lfdnr, dip_replace_matnr, dip_replace_lfdnr, dip_seqno);


--
-- Name: da_internal_text da_internal_text_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_internal_text
    ADD CONSTRAINT da_internal_text_pkey PRIMARY KEY (dit_u_id, dit_creation_date, dit_do_type, dit_do_id);


--
-- Name: da_invoice_relevance da_invoice_relevance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_invoice_relevance
    ADD CONSTRAINT da_invoice_relevance_pkey PRIMARY KEY (dir_do_type, dir_field);


--
-- Name: da_kem_masterdata da_kem_masterdata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_kem_masterdata
    ADD CONSTRAINT da_kem_masterdata_pkey PRIMARY KEY (dkm_kem, dkm_sda);


--
-- Name: da_kem_response_data da_kem_response_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_kem_response_data
    ADD CONSTRAINT da_kem_response_data_pkey PRIMARY KEY (krd_factory, krd_kem, krd_fin);


--
-- Name: da_kem_work_basket_mbs da_kem_work_basket_mbs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_kem_work_basket_mbs
    ADD CONSTRAINT da_kem_work_basket_mbs_pkey PRIMARY KEY (dkwm_kem, dkwm_saa, dkwm_group, dkwm_product_no, dkwm_kg, dkwm_module_no);


--
-- Name: da_kem_work_basket da_kem_work_basket_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_kem_work_basket
    ADD CONSTRAINT da_kem_work_basket_pkey PRIMARY KEY (dkwb_kem, dkwb_saa, dkwb_product_no, dkwb_kg, dkwb_module_no);


--
-- Name: da_kgtu_as da_kgtu_as_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_kgtu_as
    ADD CONSTRAINT da_kgtu_as_pkey PRIMARY KEY (da_dkm_product, da_dkm_kg, da_dkm_tu);


--
-- Name: da_kgtu_template da_kgtu_template_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_kgtu_template
    ADD CONSTRAINT da_kgtu_template_pkey PRIMARY KEY (da_dkt_aggregate_type, da_dkt_as_product_class, da_dkt_kg, da_dkt_tu);


--
-- Name: da_message da_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_message
    ADD CONSTRAINT da_message_pkey PRIMARY KEY (dmsg_guid);


--
-- Name: da_message_to da_message_to_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_message_to
    ADD CONSTRAINT da_message_to_pkey PRIMARY KEY (dmt_guid, dmt_user_id, dmt_group_id, dmt_organisation_id, dmt_role_id);


--
-- Name: da_model_building_code da_model_building_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_model_building_code
    ADD CONSTRAINT da_model_building_code_pkey PRIMARY KEY (dmbc_series_no, dmbc_aa, dmbc_code);


--
-- Name: da_model_data da_model_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_model_data
    ADD CONSTRAINT da_model_data_pkey PRIMARY KEY (dmd_model_no);


--
-- Name: da_model_element_usage da_model_element_usage_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_model_element_usage
    ADD CONSTRAINT da_model_element_usage_pkey PRIMARY KEY (dmeu_modelno, dmeu_module, dmeu_sub_module, dmeu_pos, dmeu_legacy_number, dmeu_revfrom);


--
-- Name: da_model_oil da_model_oil_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_model_oil
    ADD CONSTRAINT da_model_oil_pkey PRIMARY KEY (dmo_model_no, dmo_spec_validity, dmo_fluid_type);


--
-- Name: da_model_oil_quantity da_model_oil_quantity_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_model_oil_quantity
    ADD CONSTRAINT da_model_oil_quantity_pkey PRIMARY KEY (dmoq_model_no, dmoq_code_validity, dmoq_fluid_type, dmoq_ident_to, dmoq_ident_from);


--
-- Name: da_model da_model_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_model
    ADD CONSTRAINT da_model_pkey PRIMARY KEY (dm_model_no);


--
-- Name: da_model_properties da_model_properties_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_model_properties
    ADD CONSTRAINT da_model_properties_pkey PRIMARY KEY (dma_model_no, dma_data);


--
-- Name: da_models_aggs da_models_aggs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_models_aggs
    ADD CONSTRAINT da_models_aggs_pkey PRIMARY KEY (dma_model_no, dma_aggregate_no);


--
-- Name: da_module_category da_module_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_module_category
    ADD CONSTRAINT da_module_category_pkey PRIMARY KEY (dmc_module);


--
-- Name: da_module_cemat da_module_cemat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_module_cemat
    ADD CONSTRAINT da_module_cemat_pkey PRIMARY KEY (dmc_module_no, dmc_lfdnr, dmc_einpas_hg, dmc_einpas_g, dmc_einpas_tu);


--
-- Name: da_module da_module_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_module
    ADD CONSTRAINT da_module_pkey PRIMARY KEY (dm_module_no);


--
-- Name: da_modules_einpas da_modules_einpas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_modules_einpas
    ADD CONSTRAINT da_modules_einpas_pkey PRIMARY KEY (dme_product_no, dme_module_no, dme_lfdnr);


--
-- Name: da_nutzdok_annotation da_nutzdok_annotation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_nutzdok_annotation
    ADD CONSTRAINT da_nutzdok_annotation_pkey PRIMARY KEY (dna_ref_id, dna_ref_type, dna_ets, dna_date, dna_lfdnr);


--
-- Name: da_nutzdok_kem da_nutzdok_kem_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_nutzdok_kem
    ADD CONSTRAINT da_nutzdok_kem_pkey PRIMARY KEY (dnk_kem);


--
-- Name: da_nutzdok_remark da_nutzdok_remark_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_nutzdok_remark
    ADD CONSTRAINT da_nutzdok_remark_pkey PRIMARY KEY (dnr_ref_id, dnr_ref_type, dnr_id);


--
-- Name: da_nutzdok_saa da_nutzdok_saa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_nutzdok_saa
    ADD CONSTRAINT da_nutzdok_saa_pkey PRIMARY KEY (dns_saa);


--
-- Name: da_omitted_parts da_omitted_parts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_omitted_parts
    ADD CONSTRAINT da_omitted_parts_pkey PRIMARY KEY (da_op_partno);


--
-- Name: da_ops_group da_ops_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_ops_group
    ADD CONSTRAINT da_ops_group_pkey PRIMARY KEY (dog_model_no, dog_group);


--
-- Name: da_ops_scope da_ops_scope_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_ops_scope
    ADD CONSTRAINT da_ops_scope_pkey PRIMARY KEY (dos_scope);


--
-- Name: da_partslist_mbs da_partslist_mbs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_partslist_mbs
    ADD CONSTRAINT da_partslist_mbs_pkey PRIMARY KEY (dpm_snr, dpm_pos, dpm_sort, dpm_kem_from);


--
-- Name: da_pem_masterdata da_pem_masterdata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_pem_masterdata
    ADD CONSTRAINT da_pem_masterdata_pkey PRIMARY KEY (dpm_pem, dpm_factory_no);


--
-- Name: da_pic_reference da_pic_reference_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_pic_reference
    ADD CONSTRAINT da_pic_reference_pkey PRIMARY KEY (dpr_ref_id, dpr_ref_date);


--
-- Name: da_pic_to_attachment da_pic_to_attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_pic_to_attachment
    ADD CONSTRAINT da_pic_to_attachment_pkey PRIMARY KEY (da_pta_picorder, da_pta_attachment);


--
-- Name: da_picorder_attachments da_picorder_attachments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_picorder_attachments
    ADD CONSTRAINT da_picorder_attachments_pkey PRIMARY KEY (dpa_guid);


--
-- Name: da_picorder_modules da_picorder_modules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_picorder_modules
    ADD CONSTRAINT da_picorder_modules_pkey PRIMARY KEY (pom_order_guid, pom_module_no);


--
-- Name: da_picorder_parts da_picorder_parts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_picorder_parts
    ADD CONSTRAINT da_picorder_parts_pkey PRIMARY KEY (ppa_order_guid, ppa_vari, ppa_ver, ppa_lfdnr, ppa_pos, ppa_sach);


--
-- Name: da_picorder_pictures da_picorder_pictures_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_picorder_pictures
    ADD CONSTRAINT da_picorder_pictures_pkey PRIMARY KEY (pop_order_guid, pop_pic_itemid, pop_pic_itemrevid);


--
-- Name: da_picorder da_picorder_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_picorder
    ADD CONSTRAINT da_picorder_pkey PRIMARY KEY (po_order_guid);


--
-- Name: da_picorder_usage da_picorder_usage_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_picorder_usage
    ADD CONSTRAINT da_picorder_usage_pkey PRIMARY KEY (pou_order_guid, pou_product_no, pou_einpas_hg, pou_einpas_g, pou_einpas_tu, pou_kg, pou_tu);


--
-- Name: da_ppua da_ppua_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_ppua
    ADD CONSTRAINT da_ppua_pkey PRIMARY KEY (da_ppua_partno, da_ppua_region, da_ppua_series, da_ppua_entity, da_ppua_type, da_ppua_year);


--
-- Name: da_primus_include_part da_primus_include_part_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_primus_include_part
    ADD CONSTRAINT da_primus_include_part_pkey PRIMARY KEY (pip_part_no, pip_include_part_no);


--
-- Name: da_primus_replace_part da_primus_replace_part_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_primus_replace_part
    ADD CONSTRAINT da_primus_replace_part_pkey PRIMARY KEY (prp_part_no);


--
-- Name: da_product_factories da_product_factories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_product_factories
    ADD CONSTRAINT da_product_factories_pkey PRIMARY KEY (dpf_product_no, dpf_factory_no);


--
-- Name: da_product_models da_product_models_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_product_models
    ADD CONSTRAINT da_product_models_pkey PRIMARY KEY (dpm_product_no, dpm_model_no);


--
-- Name: da_product_modules da_product_modules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_product_modules
    ADD CONSTRAINT da_product_modules_pkey PRIMARY KEY (dpm_product_no, dpm_module_no);


--
-- Name: da_product da_product_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_product
    ADD CONSTRAINT da_product_pkey PRIMARY KEY (dp_product_no);


--
-- Name: da_product_sas da_product_sas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_product_sas
    ADD CONSTRAINT da_product_sas_pkey PRIMARY KEY (dps_product_no, dps_sa_no, dps_kg);


--
-- Name: da_product_series da_product_series_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_product_series
    ADD CONSTRAINT da_product_series_pkey PRIMARY KEY (dps_product_no, dps_series_no);


--
-- Name: da_pseudo_pem_date da_pseudo_pem_date_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_pseudo_pem_date
    ADD CONSTRAINT da_pseudo_pem_date_pkey PRIMARY KEY (dpd_pem_date);


--
-- Name: da_psk_product_variants da_psk_product_variants_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_psk_product_variants
    ADD CONSTRAINT da_psk_product_variants_pkey PRIMARY KEY (dppv_product_no, dppv_variant_id);


--
-- Name: da_replace_const_mat da_replace_const_mat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_replace_const_mat
    ADD CONSTRAINT da_replace_const_mat_pkey PRIMARY KEY (drcm_part_no, drcm_sdata);


--
-- Name: da_replace_const_part da_replace_const_part_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_replace_const_part
    ADD CONSTRAINT da_replace_const_part_pkey PRIMARY KEY (drcp_part_no, drcp_sdata);


--
-- Name: da_replace_part da_replace_part_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_replace_part
    ADD CONSTRAINT da_replace_part_pkey PRIMARY KEY (drp_vari, drp_ver, drp_lfdnr, drp_seqno);


--
-- Name: da_report_const_nodes da_report_const_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_report_const_nodes
    ADD CONSTRAINT da_report_const_nodes_pkey PRIMARY KEY (drcn_series_no, drcn_node_id, drcn_changeset_guid);


--
-- Name: da_reserved_pk da_reserved_pk_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_reserved_pk
    ADD CONSTRAINT da_reserved_pk_pkey PRIMARY KEY (drp_do_type, drp_do_id);


--
-- Name: da_response_data da_response_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_response_data
    ADD CONSTRAINT da_response_data_pkey PRIMARY KEY (drd_factory, drd_series_no, drd_aa, drd_bmaa, drd_pem, drd_adat, drd_ident, drd_as_data);


--
-- Name: da_response_spikes da_response_spikes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_response_spikes
    ADD CONSTRAINT da_response_spikes_pkey PRIMARY KEY (drs_factory, drs_series_no, drs_aa, drs_bmaa, drs_ident, drs_spike_ident, drs_pem, drs_adat, drs_as_data);


--
-- Name: da_sa_modules da_sa_modules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_sa_modules
    ADD CONSTRAINT da_sa_modules_pkey PRIMARY KEY (dsm_sa_no);


--
-- Name: da_sa da_sa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_sa
    ADD CONSTRAINT da_sa_pkey PRIMARY KEY (ds_sa);


--
-- Name: da_saa_history da_saa_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_saa_history
    ADD CONSTRAINT da_saa_history_pkey PRIMARY KEY (dsh_saa, dsh_rev_from);


--
-- Name: da_saa da_saa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_saa
    ADD CONSTRAINT da_saa_pkey PRIMARY KEY (ds_saa);


--
-- Name: da_scope_kg_mapping da_scope_kg_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_scope_kg_mapping
    ADD CONSTRAINT da_scope_kg_mapping_pkey PRIMARY KEY (dskm_scope_id, dskm_kg);


--
-- Name: da_series_aggs da_series_aggs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_series_aggs
    ADD CONSTRAINT da_series_aggs_pkey PRIMARY KEY (dsa_series_no, dsa_aggseries_no);


--
-- Name: da_series_codes da_series_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_series_codes
    ADD CONSTRAINT da_series_codes_pkey PRIMARY KEY (dsc_series_no, dsc_group, dsc_pos, dsc_posv, dsc_aa, dsc_sdata);


--
-- Name: da_series_events da_series_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_series_events
    ADD CONSTRAINT da_series_events_pkey PRIMARY KEY (dse_series_no, dse_event_id, dse_sdata);


--
-- Name: da_series_expdate da_series_expdate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_series_expdate
    ADD CONSTRAINT da_series_expdate_pkey PRIMARY KEY (dsed_series_no, dsed_aa, dsed_factory_no);


--
-- Name: da_series da_series_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_series
    ADD CONSTRAINT da_series_pkey PRIMARY KEY (ds_series_no);


--
-- Name: da_series_sop da_series_sop_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_series_sop
    ADD CONSTRAINT da_series_sop_pkey PRIMARY KEY (dsp_series_no, dsp_aa);


--
-- Name: da_spk_mapping da_spk_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_spk_mapping
    ADD CONSTRAINT da_spk_mapping_pkey PRIMARY KEY (spkm_series_no, spkm_hm, spkm_m, spkm_kurz_e, spkm_kurz_as, spkm_steering);


--
-- Name: da_spring_mapping da_spring_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_spring_mapping
    ADD CONSTRAINT da_spring_mapping_pkey PRIMARY KEY (dsm_zb_spring_leg);


--
-- Name: da_structure_mbs da_structure_mbs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_structure_mbs
    ADD CONSTRAINT da_structure_mbs_pkey PRIMARY KEY (dsm_snr, dsm_snr_suffix, dsm_pos, dsm_sort, dsm_kem_from);


--
-- Name: da_structure da_structure_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_structure
    ADD CONSTRAINT da_structure_pkey PRIMARY KEY (ds_parent, ds_child);


--
-- Name: da_sub_module_category da_sub_module_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_sub_module_category
    ADD CONSTRAINT da_sub_module_category_pkey PRIMARY KEY (dsmc_sub_module);


--
-- Name: da_supplier_partno_mapping da_supplier_partno_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_supplier_partno_mapping
    ADD CONSTRAINT da_supplier_partno_mapping_pkey PRIMARY KEY (dspm_partno, dspm_supplier_partno, dspm_supplier_no);


--
-- Name: da_top_tus da_top_tus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_top_tus
    ADD CONSTRAINT da_top_tus_pkey PRIMARY KEY (dtt_product_no, dtt_country_code, dtt_kg, dtt_tu);


--
-- Name: da_transit_lang_mapping da_transit_lang_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_transit_lang_mapping
    ADD CONSTRAINT da_transit_lang_mapping_pkey PRIMARY KEY (da_tlm_transit_language);


--
-- Name: da_um_groups da_um_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_um_groups
    ADD CONSTRAINT da_um_groups_pkey PRIMARY KEY (da_g_guid);


--
-- Name: da_um_roles da_um_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_um_roles
    ADD CONSTRAINT da_um_roles_pkey PRIMARY KEY (da_r_guid);


--
-- Name: da_um_user_groups da_um_user_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_um_user_groups
    ADD CONSTRAINT da_um_user_groups_pkey PRIMARY KEY (da_ug_uguid, da_ug_gguid);


--
-- Name: da_um_user_roles da_um_user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_um_user_roles
    ADD CONSTRAINT da_um_user_roles_pkey PRIMARY KEY (da_ur_uguid, da_ur_rguid);


--
-- Name: da_um_users da_um_users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_um_users
    ADD CONSTRAINT da_um_users_pkey PRIMARY KEY (da_u_guid);


--
-- Name: da_vehicle_datacard_codes da_vehicle_datacard_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_vehicle_datacard_codes
    ADD CONSTRAINT da_vehicle_datacard_codes_pkey PRIMARY KEY (dvdc_code);


--
-- Name: da_vin_model_mapping da_vin_model_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_vin_model_mapping
    ADD CONSTRAINT da_vin_model_mapping_pkey PRIMARY KEY (dvm_vin_prefix, dvm_model_prefix);


--
-- Name: da_vs2us_relation da_vs2us_relation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_vs2us_relation
    ADD CONSTRAINT da_vs2us_relation_pkey PRIMARY KEY (vur_vehicle_series, vur_vs_pos, vur_vs_posv, vur_aa, vur_unit_series, vur_data);


--
-- Name: da_wb_saa_calculation da_wb_saa_calculation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_wb_saa_calculation
    ADD CONSTRAINT da_wb_saa_calculation_pkey PRIMARY KEY (wsc_source, wsc_model_no, wsc_saa);


--
-- Name: da_wb_saa_states da_wb_saa_states_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_wb_saa_states
    ADD CONSTRAINT da_wb_saa_states_pkey PRIMARY KEY (wbs_model_no, wbs_product_no, wbs_saa, wbs_source);


--
-- Name: da_wb_supplier_mapping da_wb_supplier_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_wb_supplier_mapping
    ADD CONSTRAINT da_wb_supplier_mapping_pkey PRIMARY KEY (dwsm_model_type_id, dwsm_product_no, dwsm_kg_from);


--
-- Name: da_wh_simplified_parts da_wh_simplified_parts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_wh_simplified_parts
    ADD CONSTRAINT da_wh_simplified_parts_pkey PRIMARY KEY (dwhs_partno, dwhs_successor_partno);


--
-- Name: da_wire_harness da_wire_harness_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_wire_harness
    ADD CONSTRAINT da_wire_harness_pkey PRIMARY KEY (dwh_snr, dwh_ref, dwh_connector_no, dwh_sub_snr, dwh_pos);


--
-- Name: da_workorder da_workorder_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_workorder
    ADD CONSTRAINT da_workorder_pkey PRIMARY KEY (dwo_bst_id);


--
-- Name: da_workorder_tasks da_workorder_tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.da_workorder_tasks
    ADD CONSTRAINT da_workorder_tasks_pkey PRIMARY KEY (dwt_bst_id, dwt_lfdnr);


--
-- Name: doku doku_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.doku
    ADD CONSTRAINT doku_pkey PRIMARY KEY (d_sprach, d_nr, d_ver);


--
-- Name: dokulink dokulink_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dokulink
    ADD CONSTRAINT dokulink_pkey PRIMARY KEY (d_kvari, d_kver, d_matnr, d_mver, d_sprach, d_knoten, d_knver, d_lfdnr);


--
-- Name: dokurefs dokurefs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dokurefs
    ADD CONSTRAINT dokurefs_pkey PRIMARY KEY (dr_dokumd5, dr_file, dr_reffilename);


--
-- Name: best_h dut_convert_local_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.best_h
    ADD CONSTRAINT dut_convert_local_pkey PRIMARY KEY (b_user, b_satzid);


--
-- Name: dwarray dwarray_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dwarray
    ADD CONSTRAINT dwarray_pkey PRIMARY KEY (dwa_feld, dwa_arrayid, dwa_lfdnr);


--
-- Name: econnections econnections_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.econnections
    ADD CONSTRAINT econnections_pkey PRIMARY KEY (ec_schema, ec_schemaver, ec_itemtype, ec_itemid, ec_lfdnr);


--
-- Name: ehotspot ehotspot_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ehotspot
    ADD CONSTRAINT ehotspot_pkey PRIMARY KEY (eh_schema, eh_schemaver, eh_sheet, eh_lfdnr);


--
-- Name: eitemdata eitemdata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eitemdata
    ADD CONSTRAINT eitemdata_pkey PRIMARY KEY (ed_schema, ed_schemaver, ed_itemtype, ed_itemid, ed_key);


--
-- Name: eitems eitems_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eitems
    ADD CONSTRAINT eitems_pkey PRIMARY KEY (ei_schema, ei_schemaver, ei_itemtype, ei_itemid);


--
-- Name: elinks elinks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.elinks
    ADD CONSTRAINT elinks_pkey PRIMARY KEY (el_schema, el_schemaver, el_itemtype, el_itemid, el_lfdnr);


--
-- Name: emechlink emechlink_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.emechlink
    ADD CONSTRAINT emechlink_pkey PRIMARY KEY (em_schema, em_schemaver, em_lfdnr);


--
-- Name: enum enum_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enum
    ADD CONSTRAINT enum_pkey PRIMARY KEY (e_name, e_token);


--
-- Name: enumlink enumlink_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enumlink
    ADD CONSTRAINT enumlink_pkey PRIMARY KEY (e_feld);


--
-- Name: epartdata epartdata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.epartdata
    ADD CONSTRAINT epartdata_pkey PRIMARY KEY (ed_schema, ed_schemaver, ed_partno, ed_partver, ed_key);


--
-- Name: epartlink epartlink_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.epartlink
    ADD CONSTRAINT epartlink_pkey PRIMARY KEY (ep_schema, ep_schemaver, ep_itemtype, ep_itemid, ep_lfdnr);


--
-- Name: eparts eparts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eparts
    ADD CONSTRAINT eparts_pkey PRIMARY KEY (ep_schema, ep_schemaver, ep_partno, ep_partver);


--
-- Name: eschemaentry eschemaentry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eschemaentry
    ADD CONSTRAINT eschemaentry_pkey PRIMARY KEY (eh_schemaentry, eh_schemaentryver, eh_lang);


--
-- Name: eschemahead eschemahead_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eschemahead
    ADD CONSTRAINT eschemahead_pkey PRIMARY KEY (eh_schema, eh_schemaver);


--
-- Name: esheet esheet_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.esheet
    ADD CONSTRAINT esheet_pkey PRIMARY KEY (es_schema, es_schemaver, es_sheet);


--
-- Name: estruct estruct_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estruct
    ADD CONSTRAINT estruct_pkey PRIMARY KEY (es_key, es_keyver, es_lfdnr);


--
-- Name: etrans etrans_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.etrans
    ADD CONSTRAINT etrans_pkey PRIMARY KEY (et_schema, et_schemaver, et_textid, et_lang);


--
-- Name: etree etree_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.etree
    ADD CONSTRAINT etree_pkey PRIMARY KEY (et_schema, et_schemaver, et_key, et_lfdnr);


--
-- Name: favorites favorites_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.favorites
    ADD CONSTRAINT favorites_pkey PRIMARY KEY (f_user_id, f_type, f_lfdnr);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: groupentry groupentry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.groupentry
    ADD CONSTRAINT groupentry_pkey PRIMARY KEY (g_gid, g_uid);


--
-- Name: groupfunc groupfunc_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.groupfunc
    ADD CONSTRAINT groupfunc_pkey PRIMARY KEY (f_ugid, f_fid);


--
-- Name: icons icons_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.icons
    ADD CONSTRAINT icons_pkey PRIMARY KEY (i_icon);


--
-- Name: images images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.images
    ADD CONSTRAINT images_pkey PRIMARY KEY (i_tiffname, i_ver, i_blatt);


--
-- Name: internal_dbparams internal_dbparams_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.internal_dbparams
    ADD CONSTRAINT internal_dbparams_pkey PRIMARY KEY (dp_schema, dp_key);


--
-- Name: kapitel kapitel_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kapitel
    ADD CONSTRAINT kapitel_pkey PRIMARY KEY (k_sprach, k_knoten, k_knver, k_lfdnr);


--
-- Name: katalog katalog_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.katalog
    ADD CONSTRAINT katalog_pkey PRIMARY KEY (k_vari, k_ver, k_lfdnr);


--
-- Name: keyvalue keyvalue_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.keyvalue
    ADD CONSTRAINT keyvalue_pkey PRIMARY KEY (kv_key);


--
-- Name: links links_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.links
    ADD CONSTRAINT links_pkey PRIMARY KEY (l_images, l_ver, l_sprach, l_usage, l_lfdnr);


--
-- Name: mat mat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mat
    ADD CONSTRAINT mat_pkey PRIMARY KEY (m_matnr, m_ver);


--
-- Name: notiz notiz_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notiz
    ADD CONSTRAINT notiz_pkey PRIMARY KEY (n_userid, n_sprach, n_typ, n_kvari, n_kver, n_dver, n_klfdnr, n_lfdnr);


--
-- Name: pool pool_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pool
    ADD CONSTRAINT pool_pkey PRIMARY KEY (p_images, p_ver, p_sprach, p_usage);


--
-- Name: poolentry poolentry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.poolentry
    ADD CONSTRAINT poolentry_pkey PRIMARY KEY (pe_images, pe_ver);


--
-- Name: preise preise_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preise
    ADD CONSTRAINT preise_pkey PRIMARY KEY (p_matnr, p_mver, p_wkz, p_idkey, p_eorderno);


--
-- Name: s_chain s_chain_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.s_chain
    ADD CONSTRAINT s_chain_pkey PRIMARY KEY (sc_vari, sc_ver, sc_lfdnr, sc_matnr, sc_mver, sc_st_source, sc_st_source_ver, sc_backflag, sc_st_dest, sc_st_dest_ver);


--
-- Name: s_items s_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.s_items
    ADD CONSTRAINT s_items_pkey PRIMARY KEY (si_st_nr, si_st_ver, si_lfdnr);


--
-- Name: s_set s_set_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.s_set
    ADD CONSTRAINT s_set_pkey PRIMARY KEY (st_st_nr, st_ver);


--
-- Name: sbadr sbadr_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sbadr
    ADD CONSTRAINT sbadr_pkey PRIMARY KEY (a_id);


--
-- Name: sbdetail sbdetail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sbdetail
    ADD CONSTRAINT sbdetail_pkey PRIMARY KEY (b_user, b_satzid, b_lfdnr, b_key);


--
-- Name: sprache sprache_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sprache
    ADD CONSTRAINT sprache_pkey PRIMARY KEY (s_feld, s_textnr, s_sprach);


--
-- Name: strukt strukt_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.strukt
    ADD CONSTRAINT strukt_pkey PRIMARY KEY (s_knoten, s_ver, s_lfdnr);


--
-- Name: treeid treeid_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.treeid
    ADD CONSTRAINT treeid_pkey PRIMARY KEY (t_id);


--
-- Name: treemod treemod_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.treemod
    ADD CONSTRAINT treemod_pkey PRIMARY KEY (t_parent, t_child);


--
-- Name: u_serno u_serno_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.u_serno
    ADD CONSTRAINT u_serno_pkey PRIMARY KEY (u_serno);


--
-- Name: ua_apps ua_apps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_apps
    ADD CONSTRAINT ua_apps_pkey PRIMARY KEY (a_id);


--
-- Name: ua_news_feedback ua_news_feedback_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_news_feedback
    ADD CONSTRAINT ua_news_feedback_pkey PRIMARY KEY (nf_news_id, nf_user_id);


--
-- Name: ua_news ua_news_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_news
    ADD CONSTRAINT ua_news_pkey PRIMARY KEY (n_id);


--
-- Name: ua_news_texts ua_news_texts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_news_texts
    ADD CONSTRAINT ua_news_texts_pkey PRIMARY KEY (nt_news_id, nt_language);


--
-- Name: ua_organisation_apps ua_organisation_apps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_organisation_apps
    ADD CONSTRAINT ua_organisation_apps_pkey PRIMARY KEY (oa_organisation_id, oa_app_id);


--
-- Name: ua_organisation_properties ua_organisation_properties_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_organisation_properties
    ADD CONSTRAINT ua_organisation_properties_pkey PRIMARY KEY (op_organisation_id, op_app_id, op_key);


--
-- Name: ua_organisation_roles ua_organisation_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_organisation_roles
    ADD CONSTRAINT ua_organisation_roles_pkey PRIMARY KEY (or_organisation_id, or_role_id);


--
-- Name: ua_organisations ua_organisations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_organisations
    ADD CONSTRAINT ua_organisations_pkey PRIMARY KEY (o_id);


--
-- Name: ua_rights ua_rights_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_rights
    ADD CONSTRAINT ua_rights_pkey PRIMARY KEY (r_id);


--
-- Name: ua_role_rights ua_role_rights_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_role_rights
    ADD CONSTRAINT ua_role_rights_pkey PRIMARY KEY (rr_role_id, rr_right_id);


--
-- Name: ua_roles ua_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_roles
    ADD CONSTRAINT ua_roles_pkey PRIMARY KEY (r_id);


--
-- Name: ua_user_admin_history ua_user_admin_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_user_admin_history
    ADD CONSTRAINT ua_user_admin_history_pkey PRIMARY KEY (uah_id);


--
-- Name: ua_user_data_templates ua_user_data_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_user_data_templates
    ADD CONSTRAINT ua_user_data_templates_pkey PRIMARY KEY (udt_key);


--
-- Name: ua_user_organisations ua_user_organisations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_user_organisations
    ADD CONSTRAINT ua_user_organisations_pkey PRIMARY KEY (uo_user_id, uo_organisation_id);


--
-- Name: ua_user_prop_templates ua_user_prop_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_user_prop_templates
    ADD CONSTRAINT ua_user_prop_templates_pkey PRIMARY KEY (upt_app_id, upt_key);


--
-- Name: ua_user_properties ua_user_properties_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_user_properties
    ADD CONSTRAINT ua_user_properties_pkey PRIMARY KEY (up_user_id, up_app_id, up_org_id, up_key);


--
-- Name: ua_user_roles ua_user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_user_roles
    ADD CONSTRAINT ua_user_roles_pkey PRIMARY KEY (ur_user_id, ur_role_id);


--
-- Name: ua_users ua_users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ua_users
    ADD CONSTRAINT ua_users_pkey PRIMARY KEY (u_id);


--
-- Name: usergroup usergroup_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usergroup
    ADD CONSTRAINT usergroup_pkey PRIMARY KEY (u_ugid);


--
-- Name: usersettings usersettings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usersettings
    ADD CONSTRAINT usersettings_pkey PRIMARY KEY (us_userid);


--
-- Name: custprop1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX custprop1 ON public.custprop USING btree (c_kvari, c_kver, c_matnr, c_mver);


--
-- Name: custprop2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX custprop2 ON public.custprop USING btree (c_key);


--
-- Name: da_ao_history1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_ao_history1 ON public.da_ao_history USING btree (dah_change_user_id, dah_change_date);


--
-- Name: da_author_order1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_author_order1 ON public.da_author_order USING btree (dao_status);


--
-- Name: da_author_order2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_author_order2 ON public.da_author_order USING btree (dao_creation_user_id, dao_status);


--
-- Name: da_author_order3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_author_order3 ON public.da_author_order USING btree (dao_change_set_id);


--
-- Name: da_author_order4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_author_order4 ON public.da_author_order USING btree (dao_current_user_id);


--
-- Name: da_author_order5; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_author_order5 ON public.da_author_order USING btree (dao_current_grp_id);


--
-- Name: da_change_set1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_change_set1 ON public.da_change_set USING btree (dcs_status);


--
-- Name: da_change_set2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_change_set2 ON public.da_change_set USING btree (dcs_source, dcs_status);


--
-- Name: da_change_set_entry1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_change_set_entry1 ON public.da_change_set_entry USING btree (dce_do_type, dce_do_id);


--
-- Name: da_change_set_entry2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_change_set_entry2 ON public.da_change_set_entry USING btree (dce_do_type, dce_do_id_old);


--
-- Name: da_change_set_entry3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_change_set_entry3 ON public.da_change_set_entry USING btree (dce_do_source_guid, dce_do_type);


--
-- Name: da_change_set_entry4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_change_set_entry4 ON public.da_change_set_entry USING btree (dce_do_type, dce_edit_info);


--
-- Name: da_change_set_entry5; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_change_set_entry5 ON public.da_change_set_entry USING btree (dce_matnr, dce_do_type);


--
-- Name: da_change_set_entry_dce_do_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_change_set_entry_dce_do_id ON public.da_change_set_entry USING gin (dce_do_id public.gin_trgm_ops);


--
-- Name: da_code1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_code1 ON public.da_code USING btree (dc_sdatb);


--
-- Name: da_code2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_code2 ON public.da_code USING btree (dc_series_no, dc_pgrp, dc_source);


--
-- Name: da_colortable_factory1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_colortable_factory1 ON public.da_colortable_factory USING btree (dccf_table_id, dccf_pos, dccf_sdata);


--
-- Name: da_colortable_factory2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_colortable_factory2 ON public.da_colortable_factory USING btree (dccf_source, dccf_data_id);


--
-- Name: da_colortable_factory3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_colortable_factory3 ON public.da_colortable_factory USING btree (dccf_pema);


--
-- Name: da_colortable_factory4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_colortable_factory4 ON public.da_colortable_factory USING btree (dccf_pemb);


--
-- Name: da_colortable_factory5; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_colortable_factory5 ON public.da_colortable_factory USING btree (dccf_sdata);


--
-- Name: da_colortable_part1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_colortable_part1 ON public.da_colortable_part USING btree (dctp_part);


--
-- Name: da_colortable_part2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_colortable_part2 ON public.da_colortable_part USING btree (dctp_status);


--
-- Name: da_confirm_changes1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_confirm_changes1 ON public.da_confirm_changes USING btree (dcc_partlist_entry_id);


--
-- Name: da_confirm_changes2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_confirm_changes2 ON public.da_confirm_changes USING btree (dcc_confirmation_user);


--
-- Name: da_const_kit_content1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_const_kit_content1 ON public.da_const_kit_content USING btree (dckc_sdb, dckc_part_no);


--
-- Name: da_const_kit_content2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_const_kit_content2 ON public.da_const_kit_content USING btree (dckc_sub_part_no);


--
-- Name: da_cortex_import_data1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_cortex_import_data1 ON public.da_cortex_import_data USING btree (dci_endpoint_name, dci_status);


--
-- Name: da_dialog1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog1 ON public.da_dialog USING btree (dd_series_no, dd_hm, dd_m, dd_sm);


--
-- Name: da_dialog2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog2 ON public.da_dialog USING btree (dd_partno);


--
-- Name: da_dialog3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog3 ON public.da_dialog USING btree (dd_series_no, dd_hm, dd_m, dd_sm, dd_pose, dd_posv, dd_ww);


--
-- Name: da_dialog4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog4 ON public.da_dialog USING btree (dd_linked_factory_data_guid);


--
-- Name: da_dialog_add_data1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog_add_data1 ON public.da_dialog_add_data USING btree (dad_series_no, dad_hm, dad_m, dad_sm, dad_pose, dad_posv, dad_ww, dad_sdatb);


--
-- Name: da_dialog_add_data2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog_add_data2 ON public.da_dialog_add_data USING btree (dad_series_no, dad_hm, dad_m, dad_sm, dad_sdatb);


--
-- Name: da_dialog_add_data3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog_add_data3 ON public.da_dialog_add_data USING btree (dad_sdatb, dad_hm);


--
-- Name: da_dialog_changes1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog_changes1 ON public.da_dialog_changes USING btree (ddc_bcte);


--
-- Name: da_dialog_changes2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog_changes2 ON public.da_dialog_changes USING btree (ddc_matnr);


--
-- Name: da_dialog_changes3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog_changes3 ON public.da_dialog_changes USING btree (ddc_change_set_guid);


--
-- Name: da_dialog_changes4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog_changes4 ON public.da_dialog_changes USING btree (ddc_series_no);


--
-- Name: da_dialog_partlist_text1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dialog_partlist_text1 ON public.da_dialog_partlist_text USING btree (dd_plt_br, dd_plt_hm, dd_plt_m, dd_plt_sm);


--
-- Name: da_dict_meta1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_meta1 ON public.da_dict_meta USING btree (da_dict_meta_dialogid);


--
-- Name: da_dict_meta2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_meta2 ON public.da_dict_meta USING btree (da_dict_meta_textid);


--
-- Name: da_dict_meta3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_meta3 ON public.da_dict_meta USING btree (da_dict_meta_foreignid);


--
-- Name: da_dict_meta4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_meta4 ON public.da_dict_meta USING btree (da_dict_meta_eldasid);


--
-- Name: da_dict_meta5; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_meta5 ON public.da_dict_meta USING btree (da_dict_meta_state);


--
-- Name: da_dict_meta6; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_meta6 ON public.da_dict_meta USING btree (da_dict_meta_txtkind_id, da_dict_meta_source);


--
-- Name: da_dict_sprache1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_sprache1 ON public.da_dict_sprache USING btree (da_dict_sprache_trans_state, da_dict_sprache_textid);


--
-- Name: da_dict_trans_job1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_trans_job1 ON public.da_dict_trans_job USING btree (dtj_jobid, dtj_dest_lang);


--
-- Name: da_dict_trans_job2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_trans_job2 ON public.da_dict_trans_job USING btree (dtj_textid, dtj_jobid);


--
-- Name: da_dict_trans_job_history1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_trans_job_history1 ON public.da_dict_trans_job_history USING btree (dtjh_jobid, dtjh_dest_lang);


--
-- Name: da_dict_trans_job_history2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_dict_trans_job_history2 ON public.da_dict_trans_job_history USING btree (dtjh_textid, dtjh_jobid, dtjh_last_modified);


--
-- Name: da_eds_const_kit1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_eds_const_kit1 ON public.da_eds_const_kit USING btree (dck_guid);


--
-- Name: da_eds_const_kit2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_eds_const_kit2 ON public.da_eds_const_kit USING btree (dck_sub_snr);


--
-- Name: da_eds_const_kit3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_eds_const_kit3 ON public.da_eds_const_kit USING btree (dck_kemfrom);


--
-- Name: da_eds_const_kit4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_eds_const_kit4 ON public.da_eds_const_kit USING btree (dck_kemto);


--
-- Name: da_eds_const_props1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_eds_const_props1 ON public.da_eds_const_props USING btree (dcp_text);


--
-- Name: da_eds_model1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_eds_model1 ON public.da_eds_model USING btree (eds_msaakey, eds_modelno);


--
-- Name: da_eds_model2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_eds_model2 ON public.da_eds_model USING btree (eds_modelno, eds_msaakey);


--
-- Name: da_eds_model3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_eds_model3 ON public.da_eds_model USING btree (upper((eds_msaakey)::text) varchar_pattern_ops);


--
-- Name: da_eds_saa_models1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_eds_saa_models1 ON public.da_eds_saa_models USING btree (da_esm_model_no, da_esm_saa_no);


--
-- Name: da_error_location1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_error_location1 ON public.da_error_location USING btree (del_series_no, del_sdb);


--
-- Name: da_export_content1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_export_content1 ON public.da_export_content USING btree (dec_state);


--
-- Name: da_export_request1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_export_request1 ON public.da_export_request USING btree (der_state);


--
-- Name: da_factories1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_factories1 ON public.da_factories USING btree (df_factory_no);


--
-- Name: da_factory_data1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_factory_data1 ON public.da_factory_data USING btree (dfd_series_no, dfd_hm, dfd_m, dfd_sm, dfd_pose, dfd_posv, dfd_ww, dfd_et, dfd_aa, dfd_sdata, dfd_factory, dfd_spkz, dfd_adat, dfd_data_id);


--
-- Name: da_factory_data2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_factory_data2 ON public.da_factory_data USING btree (dfd_series_no, dfd_pema);


--
-- Name: da_factory_data3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_factory_data3 ON public.da_factory_data USING btree (dfd_series_no, dfd_pemb);


--
-- Name: da_factory_data4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_factory_data4 ON public.da_factory_data USING btree (dfd_factory, dfd_series_no, dfd_aa);


--
-- Name: da_factory_data5; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_factory_data5 ON public.da_factory_data USING btree (dfd_source, dfd_data_id, dfd_factory, dfd_pema);


--
-- Name: da_factory_data6; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_factory_data6 ON public.da_factory_data USING btree (dfd_seq_no, dfd_data_id);


--
-- Name: da_factory_data_dfd_guid_gin_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_factory_data_dfd_guid_gin_idx ON public.da_factory_data USING gin (dfd_guid public.gin_trgm_ops);


--
-- Name: da_fn1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_fn1 ON public.da_fn USING btree (dfn_name);


--
-- Name: da_fn_content1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_fn_content1 ON public.da_fn_content USING btree (dfnc_text);


--
-- Name: da_fn_katalog_ref1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_fn_katalog_ref1 ON public.da_fn_katalog_ref USING btree (dfnk_fnid);


--
-- Name: da_fn_mat_ref1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_fn_mat_ref1 ON public.da_fn_mat_ref USING btree (dfnm_fnid);


--
-- Name: da_generic_part1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_generic_part1 ON public.da_generic_part USING btree (dgp_series_no, dgp_hm, dgp_m, dgp_sm);


--
-- Name: da_generic_part2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_generic_part2 ON public.da_generic_part USING btree (dgp_partno);


--
-- Name: da_hmmsm_kgtu1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_hmmsm_kgtu1 ON public.da_hmmsm_kgtu USING btree (dhk_br_hmmsm);


--
-- Name: da_hmo_saa_mapping1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_hmo_saa_mapping1 ON public.da_hmo_saa_mapping USING btree (dhsm_saa);


--
-- Name: da_include_part1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_include_part1 ON public.da_include_part USING btree (dip_vari, dip_ver, dip_lfdnr, dip_replace_lfdnr);


--
-- Name: da_internal_text1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_internal_text1 ON public.da_internal_text USING btree (dit_do_id);


--
-- Name: da_kem_response_data1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_kem_response_data1 ON public.da_kem_response_data USING btree (krd_kem);


--
-- Name: da_kgtu_as1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_kgtu_as1 ON public.da_kgtu_as USING btree (da_dkm_desc, da_dkm_product);


--
-- Name: da_message1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_message1 ON public.da_message USING btree (dmsg_type, dmsg_do_type, dmsg_do_id);


--
-- Name: da_message_to1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_message_to1 ON public.da_message_to USING btree (dmt_user_id, dmt_group_id, dmt_organisation_id, dmt_role_id);


--
-- Name: da_message_to2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_message_to2 ON public.da_message_to USING btree (dmt_group_id);


--
-- Name: da_message_to3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_message_to3 ON public.da_message_to USING btree (dmt_organisation_id);


--
-- Name: da_message_to4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_message_to4 ON public.da_message_to USING btree (dmt_role_id);


--
-- Name: da_message_to5; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_message_to5 ON public.da_message_to USING btree (dmt_read_by_user_id);


--
-- Name: da_model1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_model1 ON public.da_model USING btree (dm_series_no);


--
-- Name: da_model_element_usage1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_model_element_usage1 ON public.da_model_element_usage USING btree (upper((dmeu_sub_element)::text) varchar_pattern_ops);


--
-- Name: da_model_properties1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_model_properties1 ON public.da_model_properties USING btree (dma_status);


--
-- Name: da_models_aggs1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_models_aggs1 ON public.da_models_aggs USING btree (dma_aggregate_no);


--
-- Name: da_models_aggs2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_models_aggs2 ON public.da_models_aggs USING btree (dma_source);


--
-- Name: da_module_cemat1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_module_cemat1 ON public.da_module_cemat USING btree (dmc_einpas_hg, dmc_einpas_g, dmc_einpas_tu);


--
-- Name: da_modules_einpas1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_modules_einpas1 ON public.da_modules_einpas USING btree (dme_product_no);


--
-- Name: da_modules_einpas2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_modules_einpas2 ON public.da_modules_einpas USING btree (dme_module_no);


--
-- Name: da_nutzdok_remark1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_nutzdok_remark1 ON public.da_nutzdok_remark USING btree (dnr_ref_type, dnr_ref_id);


--
-- Name: da_partslist_mbs1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_partslist_mbs1 ON public.da_partslist_mbs USING btree (dpm_sub_snr);


--
-- Name: da_partslist_mbs2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_partslist_mbs2 ON public.da_partslist_mbs USING btree (dpm_release_from, dpm_release_to);


--
-- Name: da_partslist_mbs3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_partslist_mbs3 ON public.da_partslist_mbs USING btree (dpm_kem_from);


--
-- Name: da_partslist_mbs4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_partslist_mbs4 ON public.da_partslist_mbs USING btree (dpm_kem_to);


--
-- Name: da_pem_masterdata1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_pem_masterdata1 ON public.da_pem_masterdata USING btree (dpm_factory_no);


--
-- Name: da_pem_masterdata2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_pem_masterdata2 ON public.da_pem_masterdata USING btree (dpm_product_no, dpm_factory_no);


--
-- Name: da_pic_reference1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_pic_reference1 ON public.da_pic_reference USING btree (dpr_mc_id, dpr_mc_rev_id);


--
-- Name: da_pic_reference2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_pic_reference2 ON public.da_pic_reference USING btree (dpr_mc_id, dpr_mc_rev_id, dpr_var_id, dpr_var_rev_id);


--
-- Name: da_pic_reference3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_pic_reference3 ON public.da_pic_reference USING btree (dpr_guid);


--
-- Name: da_picorder1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_picorder1 ON public.da_picorder USING btree (po_order_id_extern, po_order_revision_extern);


--
-- Name: da_picorder2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_picorder2 ON public.da_picorder USING btree (po_status);


--
-- Name: da_picorder_parts1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_picorder_parts1 ON public.da_picorder_parts USING btree (ppa_vari, ppa_ver, ppa_lfdnr);


--
-- Name: da_picorder_pictures1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_picorder_pictures1 ON public.da_picorder_pictures USING btree (pop_pic_itemid, pop_pic_itemrevid);


--
-- Name: da_picorder_usage1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_picorder_usage1 ON public.da_picorder_usage USING btree (pou_einpas_hg, pou_einpas_g, pou_einpas_tu);


--
-- Name: da_picorder_usage2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_picorder_usage2 ON public.da_picorder_usage USING btree (pou_kg, pou_tu);


--
-- Name: da_primus_replace_part_prp_successor_partno_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_primus_replace_part_prp_successor_partno_idx ON public.da_primus_replace_part USING btree (prp_successor_partno);


--
-- Name: da_product_models1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_product_models1 ON public.da_product_models USING btree (dpm_model_no, dpm_product_no, dpm_model_visible);


--
-- Name: da_product_models2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_product_models2 ON public.da_product_models USING btree (dpm_product_no, dpm_model_no, dpm_model_visible);


--
-- Name: da_product_modules1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_product_modules1 ON public.da_product_modules USING btree (dpm_module_no);


--
-- Name: da_product_sas1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_product_sas1 ON public.da_product_sas USING btree (dps_sa_no);


--
-- Name: da_replace_const_mat1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_replace_const_mat1 ON public.da_replace_const_mat USING btree (drcm_pre_part_no, drcm_part_no);


--
-- Name: da_replace_const_part1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_replace_const_part1 ON public.da_replace_const_part USING btree (drcp_pre_matnr);


--
-- Name: da_replace_const_part2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_replace_const_part2 ON public.da_replace_const_part USING btree (drcp_replace_matnr);


--
-- Name: da_replace_part1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_replace_part1 ON public.da_replace_part USING btree (drp_vari, drp_ver, drp_replace_lfdnr);


--
-- Name: da_replace_part2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_replace_part2 ON public.da_replace_part USING btree (drp_source_guid, drp_replace_source_guid);


--
-- Name: da_report_const_nodes1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_report_const_nodes1 ON public.da_report_const_nodes USING btree (drcn_changeset_guid);


--
-- Name: da_reserved_pk1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_reserved_pk1 ON public.da_reserved_pk USING btree (drp_change_set_id);


--
-- Name: da_response_data1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_response_data1 ON public.da_response_data USING btree (drd_pem);


--
-- Name: da_response_spikes1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_response_spikes1 ON public.da_response_spikes USING btree (drs_factory, drs_series_no, drs_aa, drs_ident, drs_pem);


--
-- Name: da_response_spikes2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_response_spikes2 ON public.da_response_spikes USING btree (drs_pem, drs_series_no);


--
-- Name: da_sa1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_sa1 ON public.da_sa USING btree (ds_desc);


--
-- Name: da_sa2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_sa2 ON public.da_sa USING btree (ds_const_desc);


--
-- Name: da_sa_modules1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_sa_modules1 ON public.da_sa_modules USING btree (dsm_module_no);


--
-- Name: da_saa1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_saa1 ON public.da_saa USING btree (ds_desc);


--
-- Name: da_saa2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_saa2 ON public.da_saa USING btree (ds_const_desc);


--
-- Name: da_saa_ds_saa_gin_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_saa_ds_saa_gin_idx ON public.da_saa USING gin (ds_saa public.gin_trgm_ops);


--
-- Name: da_series_codes1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_series_codes1 ON public.da_series_codes USING btree (dsc_series_no, dsc_sdatb);


--
-- Name: da_series_events1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_series_events1 ON public.da_series_events USING btree (dse_series_no, dse_previous_event_id, dse_sdata);


--
-- Name: da_spk_mapping1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_spk_mapping1 ON public.da_spk_mapping USING btree (spkm_kurz_as);


--
-- Name: da_structure_mbs1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_structure_mbs1 ON public.da_structure_mbs USING btree (dsm_sub_snr);


--
-- Name: da_structure_mbs2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_structure_mbs2 ON public.da_structure_mbs USING btree (dsm_release_from, dsm_release_to);


--
-- Name: da_structure_mbs3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_structure_mbs3 ON public.da_structure_mbs USING btree (dsm_snr, dsm_sub_snr);


--
-- Name: da_supplier_partno_mapping1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_supplier_partno_mapping1 ON public.da_supplier_partno_mapping USING btree (dspm_supplier_no);


--
-- Name: da_supplier_partno_mapping2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_supplier_partno_mapping2 ON public.da_supplier_partno_mapping USING btree (dspm_supplier_partno);


--
-- Name: da_supplier_partno_mapping3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_supplier_partno_mapping3 ON public.da_supplier_partno_mapping USING btree (dspm_supplier_partno_plain);


--
-- Name: da_transit_lang_mapping1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_transit_lang_mapping1 ON public.da_transit_lang_mapping USING btree (da_tlm_iso_language);


--
-- Name: da_wb_supplier_mapping1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_wb_supplier_mapping1 ON public.da_wb_supplier_mapping USING btree (dwsm_product_no);


--
-- Name: da_wb_supplier_mapping2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_wb_supplier_mapping2 ON public.da_wb_supplier_mapping USING btree (dwsm_supplier_no);


--
-- Name: da_workorder1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_workorder1 ON public.da_workorder USING btree (dwo_order_no);


--
-- Name: da_workorder2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_workorder2 ON public.da_workorder USING btree (dwo_supplier_no);


--
-- Name: dfd_guid_gin_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dfd_guid_gin_idx ON public.da_factory_data USING gin (dfd_guid public.gin_trgm_ops);


--
-- Name: doku1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX doku1 ON public.doku USING btree (d_md5);


--
-- Name: doku2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX doku2 ON public.doku USING btree (upper((d_titel)::text) varchar_pattern_ops);


--
-- Name: dokulink1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dokulink1 ON public.dokulink USING btree (d_kvari, d_kver, d_matnr, d_mver, d_vsprach, d_vknoten, d_vknver);


--
-- Name: dokulink2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dokulink2 ON public.dokulink USING btree (d_matnr, d_mver);


--
-- Name: dokulink3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dokulink3 ON public.dokulink USING btree (d_dsprach, d_nr, d_ver);


--
-- Name: dokulink4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dokulink4 ON public.dokulink USING btree (upper((d_text)::text) varchar_pattern_ops);


--
-- Name: dokurefs1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dokurefs1 ON public.dokurefs USING btree (dr_reffilename);


--
-- Name: dwa_token_gin_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dwa_token_gin_idx ON public.dwarray USING gin (dwa_token public.gin_trgm_ops);


--
-- Name: dwarray1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dwarray1 ON public.dwarray USING btree (dwa_token);


--
-- Name: dwarray2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dwarray2 ON public.dwarray USING btree (dwa_arrayid);


--
-- Name: eitems1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX eitems1 ON public.eitems USING btree (ei_schema, ei_schemaver, ei_parenttype, ei_parentid);


--
-- Name: eitems2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX eitems2 ON public.eitems USING btree (ei_name, ei_schema, ei_schemaver);


--
-- Name: epartlink1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX epartlink1 ON public.epartlink USING btree (ep_schema, ep_schemaver, ep_partno, ep_partver);


--
-- Name: eparts1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX eparts1 ON public.eparts USING btree (ep_schema, ep_schemaver, ep_linkpartno, ep_linkpartnover);


--
-- Name: eparts2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX eparts2 ON public.eparts USING btree (ep_linkpartno, ep_linkpartnover);


--
-- Name: eparts3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX eparts3 ON public.eparts USING btree (upper((ep_visiblepartno)::text) varchar_pattern_ops);


--
-- Name: etrans1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX etrans1 ON public.etrans USING btree (et_text, et_lang);


--
-- Name: favorites1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX favorites1 ON public.favorites USING btree (f_user_id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: images1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX images1 ON public.images USING btree (i_images, i_pver);


--
-- Name: kapitel1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX kapitel1 ON public.kapitel USING btree (k_dsprach, k_nr, k_nrver);


--
-- Name: kapitel2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX kapitel2 ON public.kapitel USING btree (k_vsprach, k_vknoten, k_vknver);


--
-- Name: kapitel3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX kapitel3 ON public.kapitel USING btree (upper((k_text)::text) varchar_pattern_ops);


--
-- Name: katalog1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX katalog1 ON public.katalog USING btree (k_sach, k_sver);


--
-- Name: katalog2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX katalog2 ON public.katalog USING btree (k_vari, k_ver, k_sach, k_sver);


--
-- Name: katalog3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX katalog3 ON public.katalog USING btree (k_matnr, k_mver);


--
-- Name: katalog4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX katalog4 ON public.katalog USING btree (k_source_type, k_source_context, k_source_ref1, k_source_ref2);


--
-- Name: katalog5; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX katalog5 ON public.katalog USING btree (k_source_guid, k_source_type);


--
-- Name: katalog6; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX katalog6 ON public.katalog USING btree (k_sa_validity);


--
-- Name: katalog7; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX katalog7 ON public.katalog USING btree (k_model_validity);


--
-- Name: katalog8; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX katalog8 ON public.katalog USING btree (k_was_auto_created, k_auto_created);


--
-- Name: katalog_k_source_guid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX katalog_k_source_guid ON public.katalog USING gin (k_source_guid public.gin_trgm_ops);


--
-- Name: links1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX links1 ON public.links USING btree (l_images, l_ver, l_text, l_sprach, l_usage);


--
-- Name: mat1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat1 ON public.mat USING btree (m_textnr);


--
-- Name: mat10; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat10 ON public.mat USING btree (m_image_available);


--
-- Name: mat11; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat11 ON public.mat USING btree (m_partno_basic);


--
-- Name: mat2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat2 ON public.mat USING btree (upper((m_bestnr)::text) varchar_pattern_ops);


--
-- Name: mat3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat3 ON public.mat USING btree (m_base_matnr);


--
-- Name: mat4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat4 ON public.mat USING btree (m_addtext);


--
-- Name: mat5; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat5 ON public.mat USING btree (m_const_desc);


--
-- Name: mat6; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat6 ON public.mat USING btree (m_change_desc);


--
-- Name: mat7; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat7 ON public.mat USING btree (m_noteone);


--
-- Name: mat8; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat8 ON public.mat USING btree (m_assembly);


--
-- Name: mat9; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mat9 ON public.mat USING btree (m_bestnr, m_matnr, m_ver);


--
-- Name: notiz1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX notiz1 ON public.notiz USING btree (n_userid, n_kvari, n_kver, n_dver, n_klfdnr, n_lfdnr);


--
-- Name: notiz2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX notiz2 ON public.notiz USING btree (n_typ, n_kvari, n_kver, n_dver, n_klfdnr, n_lfdnr);


--
-- Name: notiz3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX notiz3 ON public.notiz USING btree (upper((n_titel)::text) varchar_pattern_ops);


--
-- Name: notiz4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX notiz4 ON public.notiz USING btree (upper(n_text) varchar_pattern_ops);


--
-- Name: pool1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pool1 ON public.pool USING btree (p_ver);


--
-- Name: s_chain1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX s_chain1 ON public.s_chain USING btree (sc_st_dest, sc_st_dest_ver);


--
-- Name: s_items1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX s_items1 ON public.s_items USING btree (si_matnr, si_mver);


--
-- Name: sprache1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sprache1 ON public.sprache USING btree (s_textnr, s_feld, s_sprach);


--
-- Name: sprache2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sprache2 ON public.sprache USING btree (upper((s_benenn)::text) varchar_pattern_ops);


--
-- Name: sprache3; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sprache3 ON public.sprache USING btree (s_textid);


--
-- Name: sprache4; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sprache4 ON public.sprache USING btree (s_sprach, s_benenn, s_feld);


--
-- Name: sprache5; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sprache5 ON public.sprache USING btree (s_textid, s_sprach);


--
-- Name: sprache6; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sprache6 ON public.sprache USING btree (s_textid, s_feld);


--
-- Name: sprache7; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sprache7 ON public.sprache USING btree (s_feld, s_sprach, s_textnr);


--
-- Name: sprache_s_textid_gin_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sprache_s_textid_gin_idx ON public.sprache USING gin (s_textid public.gin_trgm_ops);


--
-- Name: strukt1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX strukt1 ON public.strukt USING btree (s_vknoten, s_vver);


--
-- Name: strukt2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX strukt2 ON public.strukt USING btree (s_kvari, s_kver);


--
-- Name: treeid1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX treeid1 ON public.treeid USING btree (t_vari, t_ver);


--
-- Name: treemod1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX treemod1 ON public.treemod USING btree (t_child);


--
-- Name: u_serno1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX u_serno1 ON public.u_serno USING btree (u_vin);


--
-- Name: u_serno2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX u_serno2 ON public.u_serno USING btree (u_modno, u_modver);


--
-- Name: ua_organisation_apps1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ua_organisation_apps1 ON public.ua_organisation_apps USING btree (oa_app_id);


--
-- Name: ua_organisation_properties1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ua_organisation_properties1 ON public.ua_organisation_properties USING btree (op_app_id);


--
-- Name: ua_organisation_roles1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ua_organisation_roles1 ON public.ua_organisation_roles USING btree (or_role_id);


--
-- Name: ua_role_rights1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ua_role_rights1 ON public.ua_role_rights USING btree (rr_right_id);


--
-- Name: ua_user_organisations1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ua_user_organisations1 ON public.ua_user_organisations USING btree (uo_organisation_id);


--
-- Name: ua_user_roles1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ua_user_roles1 ON public.ua_user_roles USING btree (ur_role_id);


--
-- Name: ua_users1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ua_users1 ON public.ua_users USING btree (u_name);


--
-- Name: usergroup1; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX usergroup1 ON public.usergroup USING btree (u_type);


--
-- Name: da_ao_history allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_ao_history TO author_group USING (true);


--
-- Name: da_author_order allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_author_order TO author_group USING (true);


--
-- Name: da_bom_mat_history allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_bom_mat_history TO author_group USING (true);


--
-- Name: da_change_set allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_change_set TO author_group USING (true);


--
-- Name: da_change_set_entry allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_change_set_entry TO author_group USING (true);


--
-- Name: da_change_set_info_defs allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_change_set_info_defs TO author_group USING (true);


--
-- Name: da_confirm_changes allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_confirm_changes TO author_group USING (true);


--
-- Name: da_const_kit_content allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_const_kit_content TO author_group USING (true);


--
-- Name: da_cortex_import_data allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_cortex_import_data TO author_group USING (true);


--
-- Name: da_dialog allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_dialog TO author_group USING (true);


--
-- Name: da_dialog_add_data allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_dialog_add_data TO author_group USING (true);


--
-- Name: da_dialog_changes allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_dialog_changes TO author_group USING (true);


--
-- Name: da_dialog_dsr allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_dialog_dsr TO author_group USING (true);


--
-- Name: da_dialog_pos_text allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_dialog_pos_text TO author_group USING (true);


--
-- Name: da_dict_trans_job allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_dict_trans_job TO author_group USING (true);


--
-- Name: da_dict_trans_job_history allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_dict_trans_job_history TO author_group USING (true);


--
-- Name: da_eds_const_kit allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_eds_const_kit TO author_group USING (true);


--
-- Name: da_eds_const_props allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_eds_const_props TO author_group USING (true);


--
-- Name: da_eds_mat_remarks allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_eds_mat_remarks TO author_group USING (true);


--
-- Name: da_eds_mat_ww_flags allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_eds_mat_ww_flags TO author_group USING (true);


--
-- Name: da_eds_model allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_eds_model TO author_group USING (true);


--
-- Name: da_eds_saa_remarks allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_eds_saa_remarks TO author_group USING (true);


--
-- Name: da_eds_saa_ww_flags allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_eds_saa_ww_flags TO author_group USING (true);


--
-- Name: da_einpashmmsm allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_einpashmmsm TO author_group USING (true);


--
-- Name: da_epc_fn_content allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_epc_fn_content TO author_group USING (true);


--
-- Name: da_epc_fn_katalog_ref allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_epc_fn_katalog_ref TO author_group USING (true);


--
-- Name: da_epc_fn_sa_ref allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_epc_fn_sa_ref TO author_group USING (true);


--
-- Name: da_export_content allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_export_content TO author_group USING (true);


--
-- Name: da_export_request allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_export_request TO author_group USING (true);


--
-- Name: da_generic_part allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_generic_part TO author_group USING (true);


--
-- Name: da_genvo_pairing allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_genvo_pairing TO author_group USING (true);


--
-- Name: da_genvo_supp_text allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_genvo_supp_text TO author_group USING (true);


--
-- Name: da_hmmsm allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_hmmsm TO author_group USING (true);


--
-- Name: da_hmmsm_kgtu allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_hmmsm_kgtu TO author_group USING (true);


--
-- Name: da_hmmsmdesc allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_hmmsmdesc TO author_group USING (true);


--
-- Name: da_hmo_saa_mapping allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_hmo_saa_mapping TO author_group USING (true);


--
-- Name: da_include_const_mat allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_include_const_mat TO author_group USING (true);


--
-- Name: da_internal_text allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_internal_text TO author_group USING (true);


--
-- Name: da_invoice_relevance allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_invoice_relevance TO author_group USING (true);


--
-- Name: da_kem_masterdata allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_kem_masterdata TO author_group USING (true);


--
-- Name: da_kem_work_basket allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_kem_work_basket TO author_group USING (true);


--
-- Name: da_kem_work_basket_mbs allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_kem_work_basket_mbs TO author_group USING (true);


--
-- Name: da_message allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_message TO author_group USING (true);


--
-- Name: da_message_to allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_message_to TO author_group USING (true);


--
-- Name: da_model_data allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_model_data TO author_group USING (true);


--
-- Name: da_model_element_usage allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_model_element_usage TO author_group USING (true);


--
-- Name: da_model_properties allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_model_properties TO author_group USING (true);


--
-- Name: da_module_category allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_module_category TO author_group USING (true);


--
-- Name: da_nutzdok_annotation allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_nutzdok_annotation TO author_group USING (true);


--
-- Name: da_nutzdok_kem allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_nutzdok_kem TO author_group USING (true);


--
-- Name: da_nutzdok_remark allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_nutzdok_remark TO author_group USING (true);


--
-- Name: da_nutzdok_saa allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_nutzdok_saa TO author_group USING (true);


--
-- Name: da_ops_group allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_ops_group TO author_group USING (true);


--
-- Name: da_ops_scope allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_ops_scope TO author_group USING (true);


--
-- Name: da_partslist_mbs allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_partslist_mbs TO author_group USING (true);


--
-- Name: da_pem_masterdata allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_pem_masterdata TO author_group USING (true);


--
-- Name: da_pic_reference allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_pic_reference TO author_group USING (true);


--
-- Name: da_pic_to_attachment allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_pic_to_attachment TO author_group USING (true);


--
-- Name: da_picorder allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_picorder TO author_group USING (true);


--
-- Name: da_picorder_attachments allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_picorder_attachments TO author_group USING (true);


--
-- Name: da_picorder_modules allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_picorder_modules TO author_group USING (true);


--
-- Name: da_picorder_parts allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_picorder_parts TO author_group USING (true);


--
-- Name: da_picorder_pictures allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_picorder_pictures TO author_group USING (true);


--
-- Name: da_picorder_usage allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_picorder_usage TO author_group USING (true);


--
-- Name: da_ppua allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_ppua TO author_group USING (true);


--
-- Name: da_product allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_product TO author_group USING (true);


--
-- Name: da_pseudo_pem_date allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_pseudo_pem_date TO author_group USING (true);


--
-- Name: da_replace_const_mat allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_replace_const_mat TO author_group USING (true);


--
-- Name: da_replace_const_part allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_replace_const_part TO author_group USING (true);


--
-- Name: da_report_const_nodes allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_report_const_nodes TO author_group USING (true);


--
-- Name: da_reserved_pk allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_reserved_pk TO author_group USING (true);


--
-- Name: da_saa_history allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_saa_history TO author_group USING (true);


--
-- Name: da_scope_kg_mapping allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_scope_kg_mapping TO author_group USING (true);


--
-- Name: da_structure_mbs allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_structure_mbs TO author_group USING (true);


--
-- Name: da_sub_module_category allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_sub_module_category TO author_group USING (true);


--
-- Name: da_transit_lang_mapping allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_transit_lang_mapping TO author_group USING (true);


--
-- Name: da_um_groups allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_um_groups TO author_group USING (true);


--
-- Name: da_um_roles allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_um_roles TO author_group USING (true);


--
-- Name: da_um_user_groups allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_um_user_groups TO author_group USING (true);


--
-- Name: da_um_user_roles allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_um_user_roles TO author_group USING (true);


--
-- Name: da_um_users allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_um_users TO author_group USING (true);


--
-- Name: da_wb_saa_calculation allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_wb_saa_calculation TO author_group USING (true);


--
-- Name: da_wb_saa_states allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_wb_saa_states TO author_group USING (true);


--
-- Name: da_wb_supplier_mapping allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_wb_supplier_mapping TO author_group USING (true);


--
-- Name: da_workorder allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_workorder TO author_group USING (true);


--
-- Name: da_workorder_tasks allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.da_workorder_tasks TO author_group USING (true);


--
-- Name: ua_apps allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_apps TO author_group USING (true);


--
-- Name: ua_news allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_news TO author_group USING (true);


--
-- Name: ua_news_feedback allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_news_feedback TO author_group USING (true);


--
-- Name: ua_news_texts allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_news_texts TO author_group USING (true);


--
-- Name: ua_organisation_apps allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_organisation_apps TO author_group USING (true);


--
-- Name: ua_organisation_properties allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_organisation_properties TO author_group USING (true);


--
-- Name: ua_organisation_roles allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_organisation_roles TO author_group USING (true);


--
-- Name: ua_organisations allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_organisations TO author_group USING (true);


--
-- Name: ua_rights allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_rights TO author_group USING (true);


--
-- Name: ua_role_rights allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_role_rights TO author_group USING (true);


--
-- Name: ua_roles allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_roles TO author_group USING (true);


--
-- Name: ua_user_admin_history allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_user_admin_history TO author_group USING (true);


--
-- Name: ua_user_data_templates allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_user_data_templates TO author_group USING (true);


--
-- Name: ua_user_organisations allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_user_organisations TO author_group USING (true);


--
-- Name: ua_user_prop_templates allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_user_prop_templates TO author_group USING (true);


--
-- Name: ua_user_properties allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_user_properties TO author_group USING (true);


--
-- Name: ua_user_roles allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_user_roles TO author_group USING (true);


--
-- Name: ua_users allow_author; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_author ON public.ua_users TO author_group USING (true);


--
-- Name: da_ao_history allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_ao_history TO glue_group USING (true);


--
-- Name: da_author_order allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_author_order TO glue_group USING (true);


--
-- Name: da_bom_mat_history allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_bom_mat_history TO glue_group USING (true);


--
-- Name: da_change_set allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_change_set TO glue_group USING (true);


--
-- Name: da_change_set_entry allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_change_set_entry TO glue_group USING (true);


--
-- Name: da_change_set_info_defs allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_change_set_info_defs TO glue_group USING (true);


--
-- Name: da_confirm_changes allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_confirm_changes TO glue_group USING (true);


--
-- Name: da_const_kit_content allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_const_kit_content TO glue_group USING (true);


--
-- Name: da_cortex_import_data allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_cortex_import_data TO glue_group USING (true);


--
-- Name: da_dialog allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_dialog TO glue_group USING (true);


--
-- Name: da_dialog_add_data allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_dialog_add_data TO glue_group USING (true);


--
-- Name: da_dialog_changes allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_dialog_changes TO glue_group USING (true);


--
-- Name: da_dialog_dsr allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_dialog_dsr TO glue_group USING (true);


--
-- Name: da_dialog_pos_text allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_dialog_pos_text TO glue_group USING (true);


--
-- Name: da_dict_trans_job allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_dict_trans_job TO glue_group USING (true);


--
-- Name: da_dict_trans_job_history allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_dict_trans_job_history TO glue_group USING (true);


--
-- Name: da_eds_const_kit allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_eds_const_kit TO glue_group USING (true);


--
-- Name: da_eds_const_props allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_eds_const_props TO glue_group USING (true);


--
-- Name: da_eds_mat_remarks allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_eds_mat_remarks TO glue_group USING (true);


--
-- Name: da_eds_mat_ww_flags allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_eds_mat_ww_flags TO glue_group USING (true);


--
-- Name: da_eds_model allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_eds_model TO glue_group USING (true);


--
-- Name: da_eds_saa_remarks allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_eds_saa_remarks TO glue_group USING (true);


--
-- Name: da_eds_saa_ww_flags allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_eds_saa_ww_flags TO glue_group USING (true);


--
-- Name: da_einpashmmsm allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_einpashmmsm TO glue_group USING (true);


--
-- Name: da_epc_fn_content allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_epc_fn_content TO glue_group USING (true);


--
-- Name: da_epc_fn_katalog_ref allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_epc_fn_katalog_ref TO glue_group USING (true);


--
-- Name: da_epc_fn_sa_ref allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_epc_fn_sa_ref TO glue_group USING (true);


--
-- Name: da_export_content allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_export_content TO glue_group USING (true);


--
-- Name: da_export_request allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_export_request TO glue_group USING (true);


--
-- Name: da_generic_part allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_generic_part TO glue_group USING (true);


--
-- Name: da_genvo_pairing allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_genvo_pairing TO glue_group USING (true);


--
-- Name: da_genvo_supp_text allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_genvo_supp_text TO glue_group USING (true);


--
-- Name: da_hmmsm allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_hmmsm TO glue_group USING (true);


--
-- Name: da_hmmsm_kgtu allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_hmmsm_kgtu TO glue_group USING (true);


--
-- Name: da_hmmsmdesc allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_hmmsmdesc TO glue_group USING (true);


--
-- Name: da_hmo_saa_mapping allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_hmo_saa_mapping TO glue_group USING (true);


--
-- Name: da_include_const_mat allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_include_const_mat TO glue_group USING (true);


--
-- Name: da_internal_text allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_internal_text TO glue_group USING (true);


--
-- Name: da_invoice_relevance allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_invoice_relevance TO glue_group USING (true);


--
-- Name: da_kem_masterdata allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_kem_masterdata TO glue_group USING (true);


--
-- Name: da_kem_work_basket allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_kem_work_basket TO glue_group USING (true);


--
-- Name: da_kem_work_basket_mbs allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_kem_work_basket_mbs TO glue_group USING (true);


--
-- Name: da_message allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_message TO glue_group USING (true);


--
-- Name: da_message_to allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_message_to TO glue_group USING (true);


--
-- Name: da_model_data allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_model_data TO glue_group USING (true);


--
-- Name: da_model_element_usage allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_model_element_usage TO glue_group USING (true);


--
-- Name: da_model_properties allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_model_properties TO glue_group USING (true);


--
-- Name: da_module_category allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_module_category TO glue_group USING (true);


--
-- Name: da_nutzdok_annotation allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_nutzdok_annotation TO glue_group USING (true);


--
-- Name: da_nutzdok_kem allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_nutzdok_kem TO glue_group USING (true);


--
-- Name: da_nutzdok_remark allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_nutzdok_remark TO glue_group USING (true);


--
-- Name: da_nutzdok_saa allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_nutzdok_saa TO glue_group USING (true);


--
-- Name: da_ops_group allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_ops_group TO glue_group USING (true);


--
-- Name: da_ops_scope allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_ops_scope TO glue_group USING (true);


--
-- Name: da_partslist_mbs allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_partslist_mbs TO glue_group USING (true);


--
-- Name: da_pem_masterdata allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_pem_masterdata TO glue_group USING (true);


--
-- Name: da_pic_reference allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_pic_reference TO glue_group USING (true);


--
-- Name: da_pic_to_attachment allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_pic_to_attachment TO glue_group USING (true);


--
-- Name: da_picorder allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_picorder TO glue_group USING (true);


--
-- Name: da_picorder_attachments allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_picorder_attachments TO glue_group USING (true);


--
-- Name: da_picorder_modules allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_picorder_modules TO glue_group USING (true);


--
-- Name: da_picorder_parts allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_picorder_parts TO glue_group USING (true);


--
-- Name: da_picorder_pictures allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_picorder_pictures TO glue_group USING (true);


--
-- Name: da_picorder_usage allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_picorder_usage TO glue_group USING (true);


--
-- Name: da_ppua allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_ppua TO glue_group USING (true);


--
-- Name: da_product allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_product TO glue_group USING (true);


--
-- Name: da_pseudo_pem_date allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_pseudo_pem_date TO glue_group USING (true);


--
-- Name: da_replace_const_mat allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_replace_const_mat TO glue_group USING (true);


--
-- Name: da_replace_const_part allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_replace_const_part TO glue_group USING (true);


--
-- Name: da_report_const_nodes allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_report_const_nodes TO glue_group USING (true);


--
-- Name: da_reserved_pk allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_reserved_pk TO glue_group USING (true);


--
-- Name: da_saa_history allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_saa_history TO glue_group USING (true);


--
-- Name: da_scope_kg_mapping allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_scope_kg_mapping TO glue_group USING (true);


--
-- Name: da_structure_mbs allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_structure_mbs TO glue_group USING (true);


--
-- Name: da_sub_module_category allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_sub_module_category TO glue_group USING (true);


--
-- Name: da_transit_lang_mapping allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_transit_lang_mapping TO glue_group USING (true);


--
-- Name: da_um_groups allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_um_groups TO glue_group USING (true);


--
-- Name: da_um_roles allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_um_roles TO glue_group USING (true);


--
-- Name: da_um_user_groups allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_um_user_groups TO glue_group USING (true);


--
-- Name: da_um_user_roles allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_um_user_roles TO glue_group USING (true);


--
-- Name: da_um_users allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_um_users TO glue_group USING (true);


--
-- Name: da_wb_saa_calculation allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_wb_saa_calculation TO glue_group USING (true);


--
-- Name: da_wb_saa_states allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_wb_saa_states TO glue_group USING (true);


--
-- Name: da_wb_supplier_mapping allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_wb_supplier_mapping TO glue_group USING (true);


--
-- Name: da_workorder allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_workorder TO glue_group USING (true);


--
-- Name: da_workorder_tasks allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.da_workorder_tasks TO glue_group USING (true);


--
-- Name: ua_apps allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_apps TO glue_group USING (true);


--
-- Name: ua_news allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_news TO glue_group USING (true);


--
-- Name: ua_news_feedback allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_news_feedback TO glue_group USING (true);


--
-- Name: ua_news_texts allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_news_texts TO glue_group USING (true);


--
-- Name: ua_organisation_apps allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_organisation_apps TO glue_group USING (true);


--
-- Name: ua_organisation_properties allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_organisation_properties TO glue_group USING (true);


--
-- Name: ua_organisation_roles allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_organisation_roles TO glue_group USING (true);


--
-- Name: ua_organisations allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_organisations TO glue_group USING (true);


--
-- Name: ua_rights allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_rights TO glue_group USING (true);


--
-- Name: ua_role_rights allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_role_rights TO glue_group USING (true);


--
-- Name: ua_roles allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_roles TO glue_group USING (true);


--
-- Name: ua_user_admin_history allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_user_admin_history TO glue_group USING (true);


--
-- Name: ua_user_data_templates allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_user_data_templates TO glue_group USING (true);


--
-- Name: ua_user_organisations allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_user_organisations TO glue_group USING (true);


--
-- Name: ua_user_prop_templates allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_user_prop_templates TO glue_group USING (true);


--
-- Name: ua_user_properties allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_user_properties TO glue_group USING (true);


--
-- Name: ua_user_roles allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_user_roles TO glue_group USING (true);


--
-- Name: ua_users allow_glue; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_glue ON public.ua_users TO glue_group USING (true);


--
-- Name: da_product allow_visible_retrieval; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY allow_visible_retrieval ON public.da_product FOR SELECT TO retrieval_group USING (((dp_product_visible)::text = '1'::text));


--
-- Name: da_ao_history; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_ao_history ENABLE ROW LEVEL SECURITY;

--
-- Name: da_author_order; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_author_order ENABLE ROW LEVEL SECURITY;

--
-- Name: da_bom_mat_history; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_bom_mat_history ENABLE ROW LEVEL SECURITY;

--
-- Name: da_change_set; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_change_set ENABLE ROW LEVEL SECURITY;

--
-- Name: da_change_set_entry; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_change_set_entry ENABLE ROW LEVEL SECURITY;

--
-- Name: da_change_set_info_defs; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_change_set_info_defs ENABLE ROW LEVEL SECURITY;

--
-- Name: da_confirm_changes; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_confirm_changes ENABLE ROW LEVEL SECURITY;

--
-- Name: da_const_kit_content; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_const_kit_content ENABLE ROW LEVEL SECURITY;

--
-- Name: da_cortex_import_data; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_cortex_import_data ENABLE ROW LEVEL SECURITY;

--
-- Name: da_dialog; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_dialog ENABLE ROW LEVEL SECURITY;

--
-- Name: da_dialog_add_data; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_dialog_add_data ENABLE ROW LEVEL SECURITY;

--
-- Name: da_dialog_changes; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_dialog_changes ENABLE ROW LEVEL SECURITY;

--
-- Name: da_dialog_dsr; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_dialog_dsr ENABLE ROW LEVEL SECURITY;

--
-- Name: da_dialog_pos_text; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_dialog_pos_text ENABLE ROW LEVEL SECURITY;

--
-- Name: da_dict_trans_job; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_dict_trans_job ENABLE ROW LEVEL SECURITY;

--
-- Name: da_dict_trans_job_history; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_dict_trans_job_history ENABLE ROW LEVEL SECURITY;

--
-- Name: da_eds_const_kit; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_eds_const_kit ENABLE ROW LEVEL SECURITY;

--
-- Name: da_eds_const_props; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_eds_const_props ENABLE ROW LEVEL SECURITY;

--
-- Name: da_eds_mat_remarks; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_eds_mat_remarks ENABLE ROW LEVEL SECURITY;

--
-- Name: da_eds_mat_ww_flags; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_eds_mat_ww_flags ENABLE ROW LEVEL SECURITY;

--
-- Name: da_eds_model; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_eds_model ENABLE ROW LEVEL SECURITY;

--
-- Name: da_eds_saa_remarks; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_eds_saa_remarks ENABLE ROW LEVEL SECURITY;

--
-- Name: da_eds_saa_ww_flags; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_eds_saa_ww_flags ENABLE ROW LEVEL SECURITY;

--
-- Name: da_einpashmmsm; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_einpashmmsm ENABLE ROW LEVEL SECURITY;

--
-- Name: da_epc_fn_content; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_epc_fn_content ENABLE ROW LEVEL SECURITY;

--
-- Name: da_epc_fn_katalog_ref; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_epc_fn_katalog_ref ENABLE ROW LEVEL SECURITY;

--
-- Name: da_epc_fn_sa_ref; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_epc_fn_sa_ref ENABLE ROW LEVEL SECURITY;

--
-- Name: da_export_content; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_export_content ENABLE ROW LEVEL SECURITY;

--
-- Name: da_export_request; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_export_request ENABLE ROW LEVEL SECURITY;

--
-- Name: da_generic_part; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_generic_part ENABLE ROW LEVEL SECURITY;

--
-- Name: da_genvo_pairing; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_genvo_pairing ENABLE ROW LEVEL SECURITY;

--
-- Name: da_genvo_supp_text; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_genvo_supp_text ENABLE ROW LEVEL SECURITY;

--
-- Name: da_hmmsm; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_hmmsm ENABLE ROW LEVEL SECURITY;

--
-- Name: da_hmmsm_kgtu; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_hmmsm_kgtu ENABLE ROW LEVEL SECURITY;

--
-- Name: da_hmmsmdesc; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_hmmsmdesc ENABLE ROW LEVEL SECURITY;

--
-- Name: da_hmo_saa_mapping; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_hmo_saa_mapping ENABLE ROW LEVEL SECURITY;

--
-- Name: da_include_const_mat; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_include_const_mat ENABLE ROW LEVEL SECURITY;

--
-- Name: da_internal_text; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_internal_text ENABLE ROW LEVEL SECURITY;

--
-- Name: da_invoice_relevance; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_invoice_relevance ENABLE ROW LEVEL SECURITY;

--
-- Name: da_kem_masterdata; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_kem_masterdata ENABLE ROW LEVEL SECURITY;

--
-- Name: da_kem_work_basket; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_kem_work_basket ENABLE ROW LEVEL SECURITY;

--
-- Name: da_kem_work_basket_mbs; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_kem_work_basket_mbs ENABLE ROW LEVEL SECURITY;

--
-- Name: da_message; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_message ENABLE ROW LEVEL SECURITY;

--
-- Name: da_message_to; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_message_to ENABLE ROW LEVEL SECURITY;

--
-- Name: da_model_data; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_model_data ENABLE ROW LEVEL SECURITY;

--
-- Name: da_model_element_usage; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_model_element_usage ENABLE ROW LEVEL SECURITY;

--
-- Name: da_model_properties; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_model_properties ENABLE ROW LEVEL SECURITY;

--
-- Name: da_module_category; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_module_category ENABLE ROW LEVEL SECURITY;

--
-- Name: da_nutzdok_annotation; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_nutzdok_annotation ENABLE ROW LEVEL SECURITY;

--
-- Name: da_nutzdok_kem; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_nutzdok_kem ENABLE ROW LEVEL SECURITY;

--
-- Name: da_nutzdok_remark; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_nutzdok_remark ENABLE ROW LEVEL SECURITY;

--
-- Name: da_nutzdok_saa; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_nutzdok_saa ENABLE ROW LEVEL SECURITY;

--
-- Name: da_ops_group; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_ops_group ENABLE ROW LEVEL SECURITY;

--
-- Name: da_ops_scope; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_ops_scope ENABLE ROW LEVEL SECURITY;

--
-- Name: da_partslist_mbs; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_partslist_mbs ENABLE ROW LEVEL SECURITY;

--
-- Name: da_pem_masterdata; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_pem_masterdata ENABLE ROW LEVEL SECURITY;

--
-- Name: da_pic_reference; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_pic_reference ENABLE ROW LEVEL SECURITY;

--
-- Name: da_pic_to_attachment; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_pic_to_attachment ENABLE ROW LEVEL SECURITY;

--
-- Name: da_picorder; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_picorder ENABLE ROW LEVEL SECURITY;

--
-- Name: da_picorder_attachments; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_picorder_attachments ENABLE ROW LEVEL SECURITY;

--
-- Name: da_picorder_modules; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_picorder_modules ENABLE ROW LEVEL SECURITY;

--
-- Name: da_picorder_parts; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_picorder_parts ENABLE ROW LEVEL SECURITY;

--
-- Name: da_picorder_pictures; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_picorder_pictures ENABLE ROW LEVEL SECURITY;

--
-- Name: da_picorder_usage; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_picorder_usage ENABLE ROW LEVEL SECURITY;

--
-- Name: da_ppua; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_ppua ENABLE ROW LEVEL SECURITY;

--
-- Name: da_product; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_product ENABLE ROW LEVEL SECURITY;

--
-- Name: da_pseudo_pem_date; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_pseudo_pem_date ENABLE ROW LEVEL SECURITY;

--
-- Name: da_replace_const_mat; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_replace_const_mat ENABLE ROW LEVEL SECURITY;

--
-- Name: da_replace_const_part; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_replace_const_part ENABLE ROW LEVEL SECURITY;

--
-- Name: da_report_const_nodes; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_report_const_nodes ENABLE ROW LEVEL SECURITY;

--
-- Name: da_reserved_pk; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_reserved_pk ENABLE ROW LEVEL SECURITY;

--
-- Name: da_saa_history; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_saa_history ENABLE ROW LEVEL SECURITY;

--
-- Name: da_scope_kg_mapping; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_scope_kg_mapping ENABLE ROW LEVEL SECURITY;

--
-- Name: da_structure_mbs; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_structure_mbs ENABLE ROW LEVEL SECURITY;

--
-- Name: da_sub_module_category; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_sub_module_category ENABLE ROW LEVEL SECURITY;

--
-- Name: da_transit_lang_mapping; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_transit_lang_mapping ENABLE ROW LEVEL SECURITY;

--
-- Name: da_um_groups; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_um_groups ENABLE ROW LEVEL SECURITY;

--
-- Name: da_um_roles; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_um_roles ENABLE ROW LEVEL SECURITY;

--
-- Name: da_um_user_groups; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_um_user_groups ENABLE ROW LEVEL SECURITY;

--
-- Name: da_um_user_roles; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_um_user_roles ENABLE ROW LEVEL SECURITY;

--
-- Name: da_um_users; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_um_users ENABLE ROW LEVEL SECURITY;

--
-- Name: da_wb_saa_calculation; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_wb_saa_calculation ENABLE ROW LEVEL SECURITY;

--
-- Name: da_wb_saa_states; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_wb_saa_states ENABLE ROW LEVEL SECURITY;

--
-- Name: da_wb_supplier_mapping; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_wb_supplier_mapping ENABLE ROW LEVEL SECURITY;

--
-- Name: da_workorder; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_workorder ENABLE ROW LEVEL SECURITY;

--
-- Name: da_workorder_tasks; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.da_workorder_tasks ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_apps; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_apps ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_news; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_news ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_news_feedback; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_news_feedback ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_news_texts; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_news_texts ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_organisation_apps; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_organisation_apps ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_organisation_properties; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_organisation_properties ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_organisation_roles; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_organisation_roles ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_organisations; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_organisations ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_rights; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_rights ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_role_rights; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_role_rights ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_roles; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_roles ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_user_admin_history; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_user_admin_history ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_user_data_templates; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_user_data_templates ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_user_organisations; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_user_organisations ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_user_prop_templates; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_user_prop_templates ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_user_properties; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_user_properties ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_user_roles; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_user_roles ENABLE ROW LEVEL SECURITY;

--
-- Name: ua_users; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.ua_users ENABLE ROW LEVEL SECURITY;

--
-- Name: TABLE best_h; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.best_h TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.best_h TO author_group;
GRANT SELECT ON TABLE public.best_h TO glue_group;


--
-- Name: TABLE bestell; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.bestell TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.bestell TO author_group;
GRANT SELECT ON TABLE public.bestell TO glue_group;


--
-- Name: TABLE custprop; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.custprop TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.custprop TO author_group;
GRANT SELECT ON TABLE public.custprop TO glue_group;


--
-- Name: TABLE da_ac_pc_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_ac_pc_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_ac_pc_mapping TO author_group;
GRANT SELECT ON TABLE public.da_ac_pc_mapping TO glue_group;


--
-- Name: TABLE da_ac_pc_permission_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_ac_pc_permission_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_ac_pc_permission_mapping TO author_group;
GRANT SELECT ON TABLE public.da_ac_pc_permission_mapping TO glue_group;


--
-- Name: TABLE da_acc_codes; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_acc_codes TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_acc_codes TO author_group;
GRANT SELECT ON TABLE public.da_acc_codes TO glue_group;


--
-- Name: TABLE da_agg_part_codes; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_agg_part_codes TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_agg_part_codes TO author_group;
GRANT SELECT ON TABLE public.da_agg_part_codes TO glue_group;


--
-- Name: TABLE da_aggs_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_aggs_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_aggs_mapping TO author_group;
GRANT SELECT ON TABLE public.da_aggs_mapping TO glue_group;


--
-- Name: TABLE da_ao_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_ao_history TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_ao_history TO author_group;
GRANT SELECT ON TABLE public.da_ao_history TO glue_group;


--
-- Name: TABLE da_as_codes; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_as_codes TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_as_codes TO author_group;
GRANT SELECT ON TABLE public.da_as_codes TO glue_group;


--
-- Name: TABLE da_author_order; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_author_order TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_author_order TO author_group;
GRANT SELECT ON TABLE public.da_author_order TO glue_group;


--
-- Name: TABLE da_bad_code; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_bad_code TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_bad_code TO author_group;
GRANT SELECT ON TABLE public.da_bad_code TO glue_group;


--
-- Name: TABLE da_bom_mat_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_bom_mat_history TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_bom_mat_history TO author_group;
GRANT SELECT ON TABLE public.da_bom_mat_history TO glue_group;


--
-- Name: TABLE da_branch_pc_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_branch_pc_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_branch_pc_mapping TO author_group;
GRANT SELECT ON TABLE public.da_branch_pc_mapping TO glue_group;


--
-- Name: TABLE da_change_set; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_change_set TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_change_set TO author_group;
GRANT SELECT ON TABLE public.da_change_set TO glue_group;


--
-- Name: TABLE da_change_set_entry; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_change_set_entry TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_change_set_entry TO author_group;
GRANT SELECT ON TABLE public.da_change_set_entry TO glue_group;


--
-- Name: TABLE da_change_set_info_defs; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_change_set_info_defs TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_change_set_info_defs TO author_group;
GRANT SELECT ON TABLE public.da_change_set_info_defs TO glue_group;


--
-- Name: TABLE da_code; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_code TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_code TO author_group;
GRANT SELECT ON TABLE public.da_code TO glue_group;


--
-- Name: TABLE da_code_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_code_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_code_mapping TO author_group;
GRANT SELECT ON TABLE public.da_code_mapping TO glue_group;


--
-- Name: TABLE da_color_number; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_color_number TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_color_number TO author_group;
GRANT SELECT ON TABLE public.da_color_number TO glue_group;


--
-- Name: TABLE da_colortable_content; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_colortable_content TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_colortable_content TO author_group;
GRANT SELECT ON TABLE public.da_colortable_content TO glue_group;


--
-- Name: TABLE da_colortable_data; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_colortable_data TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_colortable_data TO author_group;
GRANT SELECT ON TABLE public.da_colortable_data TO glue_group;


--
-- Name: TABLE da_colortable_factory; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_colortable_factory TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_colortable_factory TO author_group;
GRANT SELECT ON TABLE public.da_colortable_factory TO glue_group;


--
-- Name: TABLE da_colortable_part; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_colortable_part TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_colortable_part TO author_group;
GRANT SELECT ON TABLE public.da_colortable_part TO glue_group;


--
-- Name: TABLE da_comb_text; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_comb_text TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_comb_text TO author_group;
GRANT SELECT ON TABLE public.da_comb_text TO glue_group;


--
-- Name: TABLE da_confirm_changes; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_confirm_changes TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_confirm_changes TO author_group;
GRANT SELECT ON TABLE public.da_confirm_changes TO glue_group;


--
-- Name: TABLE da_const_kit_content; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_const_kit_content TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_const_kit_content TO author_group;
GRANT SELECT ON TABLE public.da_const_kit_content TO glue_group;


--
-- Name: TABLE da_const_status_codes; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_const_status_codes TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_const_status_codes TO author_group;
GRANT SELECT ON TABLE public.da_const_status_codes TO glue_group;


--
-- Name: TABLE da_cortex_import_data; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_cortex_import_data TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_cortex_import_data TO author_group;
GRANT SELECT ON TABLE public.da_cortex_import_data TO glue_group;


--
-- Name: TABLE da_country_code_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_country_code_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_country_code_mapping TO author_group;
GRANT SELECT ON TABLE public.da_country_code_mapping TO glue_group;


--
-- Name: TABLE da_country_invalid_parts; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_country_invalid_parts TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_country_invalid_parts TO author_group;
GRANT SELECT ON TABLE public.da_country_invalid_parts TO glue_group;


--
-- Name: TABLE da_country_valid_series; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_country_valid_series TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_country_valid_series TO author_group;
GRANT SELECT ON TABLE public.da_country_valid_series TO glue_group;


--
-- Name: TABLE da_dialog; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dialog TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dialog TO author_group;
GRANT SELECT ON TABLE public.da_dialog TO glue_group;


--
-- Name: TABLE da_dialog_add_data; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dialog_add_data TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dialog_add_data TO author_group;
GRANT SELECT ON TABLE public.da_dialog_add_data TO glue_group;


--
-- Name: TABLE da_dialog_changes; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dialog_changes TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dialog_changes TO author_group;
GRANT SELECT ON TABLE public.da_dialog_changes TO glue_group;


--
-- Name: TABLE da_dialog_dsr; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dialog_dsr TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dialog_dsr TO author_group;
GRANT SELECT ON TABLE public.da_dialog_dsr TO glue_group;


--
-- Name: TABLE da_dialog_partlist_text; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dialog_partlist_text TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dialog_partlist_text TO author_group;
GRANT SELECT ON TABLE public.da_dialog_partlist_text TO glue_group;


--
-- Name: TABLE da_dialog_pos_text; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dialog_pos_text TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dialog_pos_text TO author_group;
GRANT SELECT ON TABLE public.da_dialog_pos_text TO glue_group;


--
-- Name: TABLE da_dict_meta; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dict_meta TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dict_meta TO author_group;
GRANT SELECT ON TABLE public.da_dict_meta TO glue_group;


--
-- Name: TABLE da_dict_sprache; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dict_sprache TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dict_sprache TO author_group;
GRANT SELECT ON TABLE public.da_dict_sprache TO glue_group;


--
-- Name: TABLE da_dict_trans_job; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dict_trans_job TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dict_trans_job TO author_group;
GRANT SELECT ON TABLE public.da_dict_trans_job TO glue_group;


--
-- Name: TABLE da_dict_trans_job_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dict_trans_job_history TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dict_trans_job_history TO author_group;
GRANT SELECT ON TABLE public.da_dict_trans_job_history TO glue_group;


--
-- Name: TABLE da_dict_txtkind; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dict_txtkind TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dict_txtkind TO author_group;
GRANT SELECT ON TABLE public.da_dict_txtkind TO glue_group;


--
-- Name: TABLE da_dict_txtkind_usage; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_dict_txtkind_usage TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_dict_txtkind_usage TO author_group;
GRANT SELECT ON TABLE public.da_dict_txtkind_usage TO glue_group;


--
-- Name: TABLE da_eds_const_kit; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_eds_const_kit TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_eds_const_kit TO author_group;
GRANT SELECT ON TABLE public.da_eds_const_kit TO glue_group;


--
-- Name: TABLE da_eds_const_props; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_eds_const_props TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_eds_const_props TO author_group;
GRANT SELECT ON TABLE public.da_eds_const_props TO glue_group;


--
-- Name: TABLE da_eds_mat_remarks; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_eds_mat_remarks TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_eds_mat_remarks TO author_group;
GRANT SELECT ON TABLE public.da_eds_mat_remarks TO glue_group;


--
-- Name: TABLE da_eds_mat_ww_flags; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_eds_mat_ww_flags TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_eds_mat_ww_flags TO author_group;
GRANT SELECT ON TABLE public.da_eds_mat_ww_flags TO glue_group;


--
-- Name: TABLE da_eds_model; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_eds_model TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_eds_model TO author_group;
GRANT SELECT ON TABLE public.da_eds_model TO glue_group;


--
-- Name: TABLE da_eds_saa_models; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_eds_saa_models TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_eds_saa_models TO author_group;
GRANT SELECT ON TABLE public.da_eds_saa_models TO glue_group;


--
-- Name: TABLE da_eds_saa_remarks; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_eds_saa_remarks TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_eds_saa_remarks TO author_group;
GRANT SELECT ON TABLE public.da_eds_saa_remarks TO glue_group;


--
-- Name: TABLE da_eds_saa_ww_flags; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_eds_saa_ww_flags TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_eds_saa_ww_flags TO author_group;
GRANT SELECT ON TABLE public.da_eds_saa_ww_flags TO glue_group;


--
-- Name: TABLE da_einpas; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_einpas TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_einpas TO author_group;
GRANT SELECT ON TABLE public.da_einpas TO glue_group;


--
-- Name: TABLE da_einpasdsc; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_einpasdsc TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_einpasdsc TO author_group;
GRANT SELECT ON TABLE public.da_einpasdsc TO glue_group;


--
-- Name: TABLE da_einpashmmsm; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_einpashmmsm TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_einpashmmsm TO author_group;
GRANT SELECT ON TABLE public.da_einpashmmsm TO glue_group;


--
-- Name: TABLE da_einpaskgtu; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_einpaskgtu TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_einpaskgtu TO author_group;
GRANT SELECT ON TABLE public.da_einpaskgtu TO glue_group;


--
-- Name: TABLE da_einpasops; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_einpasops TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_einpasops TO author_group;
GRANT SELECT ON TABLE public.da_einpasops TO glue_group;


--
-- Name: TABLE da_epc_fn_content; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_epc_fn_content TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_epc_fn_content TO author_group;
GRANT SELECT ON TABLE public.da_epc_fn_content TO glue_group;


--
-- Name: TABLE da_epc_fn_katalog_ref; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_epc_fn_katalog_ref TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_epc_fn_katalog_ref TO author_group;
GRANT SELECT ON TABLE public.da_epc_fn_katalog_ref TO glue_group;


--
-- Name: TABLE da_epc_fn_sa_ref; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_epc_fn_sa_ref TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_epc_fn_sa_ref TO author_group;
GRANT SELECT ON TABLE public.da_epc_fn_sa_ref TO glue_group;


--
-- Name: TABLE da_error_location; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_error_location TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_error_location TO author_group;
GRANT SELECT ON TABLE public.da_error_location TO glue_group;


--
-- Name: TABLE da_es1; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_es1 TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_es1 TO author_group;
GRANT SELECT ON TABLE public.da_es1 TO glue_group;


--
-- Name: TABLE da_export_content; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_export_content TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_export_content TO author_group;
GRANT SELECT ON TABLE public.da_export_content TO glue_group;


--
-- Name: TABLE da_export_request; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_export_request TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_export_request TO author_group;
GRANT SELECT ON TABLE public.da_export_request TO glue_group;


--
-- Name: TABLE da_factories; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_factories TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_factories TO author_group;
GRANT SELECT ON TABLE public.da_factories TO glue_group;


--
-- Name: TABLE da_factory_data; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_factory_data TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_factory_data TO author_group;
GRANT SELECT ON TABLE public.da_factory_data TO glue_group;


--
-- Name: TABLE da_factory_model; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_factory_model TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_factory_model TO author_group;
GRANT SELECT ON TABLE public.da_factory_model TO glue_group;


--
-- Name: TABLE da_fn; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_fn TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_fn TO author_group;
GRANT SELECT ON TABLE public.da_fn TO glue_group;


--
-- Name: TABLE da_fn_content; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_fn_content TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_fn_content TO author_group;
GRANT SELECT ON TABLE public.da_fn_content TO glue_group;


--
-- Name: TABLE da_fn_katalog_ref; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_fn_katalog_ref TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_fn_katalog_ref TO author_group;
GRANT SELECT ON TABLE public.da_fn_katalog_ref TO glue_group;


--
-- Name: TABLE da_fn_mat_ref; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_fn_mat_ref TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_fn_mat_ref TO author_group;
GRANT SELECT ON TABLE public.da_fn_mat_ref TO glue_group;


--
-- Name: TABLE da_fn_pos; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_fn_pos TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_fn_pos TO author_group;
GRANT SELECT ON TABLE public.da_fn_pos TO glue_group;


--
-- Name: TABLE da_fn_saa_ref; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_fn_saa_ref TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_fn_saa_ref TO author_group;
GRANT SELECT ON TABLE public.da_fn_saa_ref TO glue_group;


--
-- Name: TABLE da_generic_install_location; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_generic_install_location TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_generic_install_location TO author_group;
GRANT SELECT ON TABLE public.da_generic_install_location TO glue_group;


--
-- Name: TABLE da_generic_part; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_generic_part TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_generic_part TO author_group;
GRANT SELECT ON TABLE public.da_generic_part TO glue_group;


--
-- Name: TABLE da_genvo_pairing; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_genvo_pairing TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_genvo_pairing TO author_group;
GRANT SELECT ON TABLE public.da_genvo_pairing TO glue_group;


--
-- Name: TABLE da_genvo_supp_text; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_genvo_supp_text TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_genvo_supp_text TO author_group;
GRANT SELECT ON TABLE public.da_genvo_supp_text TO glue_group;


--
-- Name: TABLE da_hmmsm; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_hmmsm TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_hmmsm TO author_group;
GRANT SELECT ON TABLE public.da_hmmsm TO glue_group;


--
-- Name: TABLE da_hmmsm_kgtu; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_hmmsm_kgtu TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_hmmsm_kgtu TO author_group;
GRANT SELECT ON TABLE public.da_hmmsm_kgtu TO glue_group;


--
-- Name: TABLE da_hmmsmdesc; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_hmmsmdesc TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_hmmsmdesc TO author_group;
GRANT SELECT ON TABLE public.da_hmmsmdesc TO glue_group;


--
-- Name: TABLE da_hmo_saa_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_hmo_saa_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_hmo_saa_mapping TO author_group;
GRANT SELECT ON TABLE public.da_hmo_saa_mapping TO glue_group;


--
-- Name: TABLE da_include_const_mat; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_include_const_mat TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_include_const_mat TO author_group;
GRANT SELECT ON TABLE public.da_include_const_mat TO glue_group;


--
-- Name: TABLE da_include_part; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_include_part TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_include_part TO author_group;
GRANT SELECT ON TABLE public.da_include_part TO glue_group;


--
-- Name: TABLE da_internal_text; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_internal_text TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_internal_text TO author_group;
GRANT SELECT ON TABLE public.da_internal_text TO glue_group;


--
-- Name: TABLE da_invoice_relevance; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_invoice_relevance TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_invoice_relevance TO author_group;
GRANT SELECT ON TABLE public.da_invoice_relevance TO glue_group;


--
-- Name: TABLE da_kem_masterdata; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_kem_masterdata TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_kem_masterdata TO author_group;
GRANT SELECT ON TABLE public.da_kem_masterdata TO glue_group;


--
-- Name: TABLE da_kem_response_data; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_kem_response_data TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_kem_response_data TO author_group;
GRANT SELECT ON TABLE public.da_kem_response_data TO glue_group;


--
-- Name: TABLE da_kem_work_basket; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_kem_work_basket TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_kem_work_basket TO author_group;
GRANT SELECT ON TABLE public.da_kem_work_basket TO glue_group;


--
-- Name: TABLE da_kem_work_basket_mbs; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_kem_work_basket_mbs TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_kem_work_basket_mbs TO author_group;
GRANT SELECT ON TABLE public.da_kem_work_basket_mbs TO glue_group;


--
-- Name: TABLE da_kgtu_as; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_kgtu_as TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_kgtu_as TO author_group;
GRANT SELECT ON TABLE public.da_kgtu_as TO glue_group;


--
-- Name: TABLE da_kgtu_template; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_kgtu_template TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_kgtu_template TO author_group;
GRANT SELECT ON TABLE public.da_kgtu_template TO glue_group;


--
-- Name: TABLE da_message; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_message TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_message TO author_group;
GRANT SELECT ON TABLE public.da_message TO glue_group;


--
-- Name: TABLE da_message_to; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_message_to TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_message_to TO author_group;
GRANT SELECT ON TABLE public.da_message_to TO glue_group;


--
-- Name: TABLE da_model; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_model TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_model TO author_group;
GRANT SELECT ON TABLE public.da_model TO glue_group;


--
-- Name: TABLE da_model_building_code; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_model_building_code TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_model_building_code TO author_group;
GRANT SELECT ON TABLE public.da_model_building_code TO glue_group;


--
-- Name: TABLE da_model_data; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_model_data TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_model_data TO author_group;
GRANT SELECT ON TABLE public.da_model_data TO glue_group;


--
-- Name: TABLE da_model_element_usage; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_model_element_usage TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_model_element_usage TO author_group;
GRANT SELECT ON TABLE public.da_model_element_usage TO glue_group;


--
-- Name: TABLE da_model_oil; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_model_oil TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_model_oil TO author_group;
GRANT SELECT ON TABLE public.da_model_oil TO glue_group;


--
-- Name: TABLE da_model_oil_quantity; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_model_oil_quantity TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_model_oil_quantity TO author_group;
GRANT SELECT ON TABLE public.da_model_oil_quantity TO glue_group;


--
-- Name: TABLE da_model_properties; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_model_properties TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_model_properties TO author_group;
GRANT SELECT ON TABLE public.da_model_properties TO glue_group;


--
-- Name: TABLE da_models_aggs; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_models_aggs TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_models_aggs TO author_group;
GRANT SELECT ON TABLE public.da_models_aggs TO glue_group;


--
-- Name: TABLE da_module; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_module TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_module TO author_group;
GRANT SELECT ON TABLE public.da_module TO glue_group;


--
-- Name: TABLE da_module_category; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_module_category TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_module_category TO author_group;
GRANT SELECT ON TABLE public.da_module_category TO glue_group;


--
-- Name: TABLE da_module_cemat; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_module_cemat TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_module_cemat TO author_group;
GRANT SELECT ON TABLE public.da_module_cemat TO glue_group;


--
-- Name: TABLE da_modules_einpas; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_modules_einpas TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_modules_einpas TO author_group;
GRANT SELECT ON TABLE public.da_modules_einpas TO glue_group;


--
-- Name: TABLE da_nutzdok_annotation; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_nutzdok_annotation TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_nutzdok_annotation TO author_group;
GRANT SELECT ON TABLE public.da_nutzdok_annotation TO glue_group;


--
-- Name: TABLE da_nutzdok_kem; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_nutzdok_kem TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_nutzdok_kem TO author_group;
GRANT SELECT ON TABLE public.da_nutzdok_kem TO glue_group;


--
-- Name: TABLE da_nutzdok_remark; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_nutzdok_remark TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_nutzdok_remark TO author_group;
GRANT SELECT ON TABLE public.da_nutzdok_remark TO glue_group;


--
-- Name: TABLE da_nutzdok_saa; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_nutzdok_saa TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_nutzdok_saa TO author_group;
GRANT SELECT ON TABLE public.da_nutzdok_saa TO glue_group;


--
-- Name: TABLE da_omitted_parts; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_omitted_parts TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_omitted_parts TO author_group;
GRANT SELECT ON TABLE public.da_omitted_parts TO glue_group;


--
-- Name: TABLE da_ops_group; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_ops_group TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_ops_group TO author_group;
GRANT SELECT ON TABLE public.da_ops_group TO glue_group;


--
-- Name: TABLE da_ops_scope; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_ops_scope TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_ops_scope TO author_group;
GRANT SELECT ON TABLE public.da_ops_scope TO glue_group;


--
-- Name: TABLE da_partslist_mbs; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_partslist_mbs TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_partslist_mbs TO author_group;
GRANT SELECT ON TABLE public.da_partslist_mbs TO glue_group;


--
-- Name: TABLE da_pem_masterdata; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_pem_masterdata TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_pem_masterdata TO author_group;
GRANT SELECT ON TABLE public.da_pem_masterdata TO glue_group;


--
-- Name: TABLE da_pic_reference; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_pic_reference TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_pic_reference TO author_group;
GRANT SELECT ON TABLE public.da_pic_reference TO glue_group;


--
-- Name: TABLE da_pic_to_attachment; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_pic_to_attachment TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_pic_to_attachment TO author_group;
GRANT SELECT ON TABLE public.da_pic_to_attachment TO glue_group;


--
-- Name: TABLE da_picorder; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_picorder TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_picorder TO author_group;
GRANT SELECT ON TABLE public.da_picorder TO glue_group;


--
-- Name: TABLE da_picorder_attachments; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_picorder_attachments TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_picorder_attachments TO author_group;
GRANT SELECT ON TABLE public.da_picorder_attachments TO glue_group;


--
-- Name: TABLE da_picorder_modules; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_picorder_modules TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_picorder_modules TO author_group;
GRANT SELECT ON TABLE public.da_picorder_modules TO glue_group;


--
-- Name: TABLE da_picorder_parts; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_picorder_parts TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_picorder_parts TO author_group;
GRANT SELECT ON TABLE public.da_picorder_parts TO glue_group;


--
-- Name: TABLE da_picorder_pictures; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_picorder_pictures TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_picorder_pictures TO author_group;
GRANT SELECT ON TABLE public.da_picorder_pictures TO glue_group;


--
-- Name: TABLE da_picorder_usage; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_picorder_usage TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_picorder_usage TO author_group;
GRANT SELECT ON TABLE public.da_picorder_usage TO glue_group;


--
-- Name: TABLE da_ppua; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_ppua TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_ppua TO author_group;
GRANT SELECT ON TABLE public.da_ppua TO glue_group;


--
-- Name: TABLE da_primus_include_part; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_primus_include_part TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_primus_include_part TO author_group;
GRANT SELECT ON TABLE public.da_primus_include_part TO glue_group;


--
-- Name: TABLE da_primus_replace_part; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_primus_replace_part TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_primus_replace_part TO author_group;
GRANT SELECT ON TABLE public.da_primus_replace_part TO glue_group;


--
-- Name: TABLE da_product; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_product TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_product TO author_group;
GRANT SELECT ON TABLE public.da_product TO glue_group;


--
-- Name: TABLE da_product_factories; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_product_factories TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_product_factories TO author_group;
GRANT SELECT ON TABLE public.da_product_factories TO glue_group;


--
-- Name: TABLE da_product_models; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_product_models TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_product_models TO author_group;
GRANT SELECT ON TABLE public.da_product_models TO glue_group;


--
-- Name: TABLE da_product_modules; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_product_modules TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_product_modules TO author_group;
GRANT SELECT ON TABLE public.da_product_modules TO glue_group;


--
-- Name: TABLE da_product_sas; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_product_sas TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_product_sas TO author_group;
GRANT SELECT ON TABLE public.da_product_sas TO glue_group;


--
-- Name: TABLE da_product_series; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_product_series TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_product_series TO author_group;
GRANT SELECT ON TABLE public.da_product_series TO glue_group;


--
-- Name: TABLE da_pseudo_pem_date; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_pseudo_pem_date TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_pseudo_pem_date TO author_group;
GRANT SELECT ON TABLE public.da_pseudo_pem_date TO glue_group;


--
-- Name: TABLE da_psk_product_variants; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_psk_product_variants TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_psk_product_variants TO author_group;
GRANT SELECT ON TABLE public.da_psk_product_variants TO glue_group;


--
-- Name: TABLE da_replace_const_mat; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_replace_const_mat TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_replace_const_mat TO author_group;
GRANT SELECT ON TABLE public.da_replace_const_mat TO glue_group;


--
-- Name: TABLE da_replace_const_part; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_replace_const_part TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_replace_const_part TO author_group;
GRANT SELECT ON TABLE public.da_replace_const_part TO glue_group;


--
-- Name: TABLE da_replace_part; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_replace_part TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_replace_part TO author_group;
GRANT SELECT ON TABLE public.da_replace_part TO glue_group;


--
-- Name: TABLE da_report_const_nodes; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_report_const_nodes TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_report_const_nodes TO author_group;
GRANT SELECT ON TABLE public.da_report_const_nodes TO glue_group;


--
-- Name: TABLE da_reserved_pk; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_reserved_pk TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_reserved_pk TO author_group;
GRANT SELECT ON TABLE public.da_reserved_pk TO glue_group;


--
-- Name: TABLE da_response_data; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_response_data TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_response_data TO author_group;
GRANT SELECT ON TABLE public.da_response_data TO glue_group;


--
-- Name: TABLE da_response_spikes; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_response_spikes TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_response_spikes TO author_group;
GRANT SELECT ON TABLE public.da_response_spikes TO glue_group;


--
-- Name: TABLE da_sa; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_sa TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_sa TO author_group;
GRANT SELECT ON TABLE public.da_sa TO glue_group;


--
-- Name: TABLE da_sa_modules; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_sa_modules TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_sa_modules TO author_group;
GRANT SELECT ON TABLE public.da_sa_modules TO glue_group;


--
-- Name: TABLE da_saa; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_saa TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_saa TO author_group;
GRANT SELECT ON TABLE public.da_saa TO glue_group;


--
-- Name: TABLE da_saa_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_saa_history TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_saa_history TO author_group;
GRANT SELECT ON TABLE public.da_saa_history TO glue_group;


--
-- Name: TABLE da_scope_kg_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_scope_kg_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_scope_kg_mapping TO author_group;
GRANT SELECT ON TABLE public.da_scope_kg_mapping TO glue_group;


--
-- Name: TABLE da_series; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_series TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_series TO author_group;
GRANT SELECT ON TABLE public.da_series TO glue_group;


--
-- Name: TABLE da_series_aggs; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_series_aggs TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_series_aggs TO author_group;
GRANT SELECT ON TABLE public.da_series_aggs TO glue_group;


--
-- Name: TABLE da_series_codes; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_series_codes TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_series_codes TO author_group;
GRANT SELECT ON TABLE public.da_series_codes TO glue_group;


--
-- Name: TABLE da_series_events; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_series_events TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_series_events TO author_group;
GRANT SELECT ON TABLE public.da_series_events TO glue_group;


--
-- Name: TABLE da_series_expdate; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_series_expdate TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_series_expdate TO author_group;
GRANT SELECT ON TABLE public.da_series_expdate TO glue_group;


--
-- Name: TABLE da_series_sop; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_series_sop TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_series_sop TO author_group;
GRANT SELECT ON TABLE public.da_series_sop TO glue_group;


--
-- Name: TABLE da_spk_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_spk_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_spk_mapping TO author_group;
GRANT SELECT ON TABLE public.da_spk_mapping TO glue_group;


--
-- Name: TABLE da_spring_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_spring_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_spring_mapping TO author_group;
GRANT SELECT ON TABLE public.da_spring_mapping TO glue_group;


--
-- Name: TABLE da_structure; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_structure TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_structure TO author_group;
GRANT SELECT ON TABLE public.da_structure TO glue_group;


--
-- Name: TABLE da_structure_mbs; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_structure_mbs TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_structure_mbs TO author_group;
GRANT SELECT ON TABLE public.da_structure_mbs TO glue_group;


--
-- Name: TABLE da_sub_module_category; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_sub_module_category TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_sub_module_category TO author_group;
GRANT SELECT ON TABLE public.da_sub_module_category TO glue_group;


--
-- Name: TABLE da_supplier_partno_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_supplier_partno_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_supplier_partno_mapping TO author_group;
GRANT SELECT ON TABLE public.da_supplier_partno_mapping TO glue_group;


--
-- Name: TABLE da_top_tus; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_top_tus TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_top_tus TO author_group;
GRANT SELECT ON TABLE public.da_top_tus TO glue_group;


--
-- Name: TABLE da_transit_lang_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_transit_lang_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_transit_lang_mapping TO author_group;
GRANT SELECT ON TABLE public.da_transit_lang_mapping TO glue_group;


--
-- Name: TABLE da_um_groups; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_um_groups TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_um_groups TO author_group;
GRANT SELECT ON TABLE public.da_um_groups TO glue_group;


--
-- Name: TABLE da_um_roles; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_um_roles TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_um_roles TO author_group;
GRANT SELECT ON TABLE public.da_um_roles TO glue_group;


--
-- Name: TABLE da_um_user_groups; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_um_user_groups TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_um_user_groups TO author_group;
GRANT SELECT ON TABLE public.da_um_user_groups TO glue_group;


--
-- Name: TABLE da_um_user_roles; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_um_user_roles TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_um_user_roles TO author_group;
GRANT SELECT ON TABLE public.da_um_user_roles TO glue_group;


--
-- Name: TABLE da_um_users; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_um_users TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_um_users TO author_group;
GRANT SELECT ON TABLE public.da_um_users TO glue_group;


--
-- Name: TABLE da_vehicle_datacard_codes; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_vehicle_datacard_codes TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_vehicle_datacard_codes TO author_group;
GRANT SELECT ON TABLE public.da_vehicle_datacard_codes TO glue_group;


--
-- Name: TABLE da_vin_model_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_vin_model_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_vin_model_mapping TO author_group;
GRANT SELECT ON TABLE public.da_vin_model_mapping TO glue_group;


--
-- Name: TABLE da_vs2us_relation; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_vs2us_relation TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_vs2us_relation TO author_group;
GRANT SELECT ON TABLE public.da_vs2us_relation TO glue_group;


--
-- Name: TABLE da_wb_saa_calculation; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_wb_saa_calculation TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_wb_saa_calculation TO author_group;
GRANT SELECT ON TABLE public.da_wb_saa_calculation TO glue_group;


--
-- Name: TABLE da_wb_saa_states; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_wb_saa_states TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_wb_saa_states TO author_group;
GRANT SELECT ON TABLE public.da_wb_saa_states TO glue_group;


--
-- Name: TABLE da_wb_supplier_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_wb_supplier_mapping TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_wb_supplier_mapping TO author_group;
GRANT SELECT ON TABLE public.da_wb_supplier_mapping TO glue_group;


--
-- Name: TABLE da_wh_simplified_parts; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_wh_simplified_parts TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_wh_simplified_parts TO author_group;
GRANT SELECT ON TABLE public.da_wh_simplified_parts TO glue_group;


--
-- Name: TABLE da_wire_harness; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_wire_harness TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_wire_harness TO author_group;
GRANT SELECT ON TABLE public.da_wire_harness TO glue_group;


--
-- Name: TABLE da_workorder; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_workorder TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_workorder TO author_group;
GRANT SELECT ON TABLE public.da_workorder TO glue_group;


--
-- Name: TABLE da_workorder_tasks; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.da_workorder_tasks TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.da_workorder_tasks TO author_group;
GRANT SELECT ON TABLE public.da_workorder_tasks TO glue_group;


--
-- Name: TABLE doku; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.doku TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.doku TO author_group;
GRANT SELECT ON TABLE public.doku TO glue_group;


--
-- Name: TABLE dokulink; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dokulink TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dokulink TO author_group;
GRANT SELECT ON TABLE public.dokulink TO glue_group;


--
-- Name: TABLE dokurefs; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dokurefs TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dokurefs TO author_group;
GRANT SELECT ON TABLE public.dokurefs TO glue_group;


--
-- Name: TABLE dtag_da_code_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_code_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_code_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_code_view TO author_group;


--
-- Name: TABLE dtag_da_color_number_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_color_number_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_color_number_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_color_number_view TO author_group;


--
-- Name: TABLE dtag_da_product_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_product_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_product_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_product_view TO author_group;


--
-- Name: TABLE dtag_da_module_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_module_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_module_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_module_view TO author_group;


--
-- Name: TABLE dtag_da_comb_text_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_comb_text_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_comb_text_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_comb_text_view TO author_group;


--
-- Name: TABLE dtag_da_dict_meta_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_dict_meta_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_dict_meta_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_dict_meta_view TO author_group;


--
-- Name: TABLE dtag_da_factory_data_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_factory_data_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_factory_data_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_factory_data_view TO author_group;


--
-- Name: TABLE dtag_da_fn_content_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_fn_content_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_fn_content_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_fn_content_view TO author_group;


--
-- Name: TABLE dtag_da_fn_kat_ref_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_fn_kat_ref_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_fn_kat_ref_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_fn_kat_ref_view TO author_group;


--
-- Name: TABLE dtag_da_fn_mat_ref_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_fn_mat_ref_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_fn_mat_ref_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_fn_mat_ref_view TO author_group;


--
-- Name: TABLE dtag_da_fn_saa_ref_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_fn_saa_ref_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_fn_saa_ref_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_fn_saa_ref_view TO author_group;


--
-- Name: TABLE dtag_da_fn_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_fn_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_fn_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_fn_view TO author_group;


--
-- Name: TABLE dtag_da_include_part_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_include_part_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_include_part_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_include_part_view TO author_group;


--
-- Name: TABLE dtag_da_kgtu_as_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_kgtu_as_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_kgtu_as_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_kgtu_as_view TO author_group;


--
-- Name: TABLE dtag_da_model_aggs_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_model_aggs_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_model_aggs_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_model_aggs_view TO author_group;


--
-- Name: TABLE dtag_da_model_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_model_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_model_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_model_view TO author_group;


--
-- Name: TABLE dtag_da_module_cemat_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_module_cemat_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_module_cemat_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_module_cemat_view TO author_group;


--
-- Name: TABLE dtag_da_modules_einpas_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_modules_einpas_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_modules_einpas_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_modules_einpas_view TO author_group;


--
-- Name: TABLE dtag_da_product_factories_plant_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_product_factories_plant_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_product_factories_plant_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_product_factories_plant_view TO author_group;


--
-- Name: TABLE dtag_da_product_factories_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_product_factories_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_product_factories_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_product_factories_view TO author_group;


--
-- Name: TABLE dtag_da_product_models_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_product_models_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_product_models_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_product_models_view TO author_group;


--
-- Name: TABLE dtag_da_product_modules_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_product_modules_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_product_modules_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_product_modules_view TO author_group;


--
-- Name: TABLE dtag_da_product_sas_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_product_sas_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_product_sas_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_product_sas_view TO author_group;


--
-- Name: TABLE dtag_da_replcae_part_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_replcae_part_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_replcae_part_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_replcae_part_view TO author_group;


--
-- Name: TABLE dtag_da_response_data_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_response_data_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_response_data_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_response_data_view TO author_group;


--
-- Name: TABLE dwarray; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dwarray TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dwarray TO author_group;
GRANT SELECT ON TABLE public.dwarray TO glue_group;


--
-- Name: TABLE katalog; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.katalog TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.katalog TO author_group;
GRANT SELECT ON TABLE public.katalog TO glue_group;


--
-- Name: TABLE dtag_da_sa_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_sa_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_sa_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_sa_view TO author_group;


--
-- Name: TABLE dtag_da_saa_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_saa_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_saa_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_saa_view TO author_group;


--
-- Name: TABLE dtag_da_top_tus_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_top_tus_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_top_tus_view TO author_group;


--
-- Name: TABLE dtag_da_vin_model_mapping_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_da_vin_model_mapping_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_da_vin_model_mapping_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_da_vin_model_mapping_view TO author_group;


--
-- Name: TABLE dtag_dwarray_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_dwarray_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_dwarray_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_dwarray_view TO author_group;


--
-- Name: TABLE images; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.images TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.images TO author_group;
GRANT SELECT ON TABLE public.images TO glue_group;


--
-- Name: TABLE dtag_images_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_images_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_images_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_images_view TO author_group;


--
-- Name: TABLE dtag_katalog_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_katalog_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_katalog_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_katalog_view TO author_group;


--
-- Name: TABLE links; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.links TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.links TO author_group;
GRANT SELECT ON TABLE public.links TO glue_group;


--
-- Name: TABLE dtag_links_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_links_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_links_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_links_view TO author_group;


--
-- Name: TABLE mat; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mat TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mat TO author_group;
GRANT SELECT ON TABLE public.mat TO glue_group;


--
-- Name: TABLE dtag_mat_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_mat_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_mat_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_mat_view TO author_group;


--
-- Name: TABLE pool; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.pool TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.pool TO author_group;
GRANT SELECT ON TABLE public.pool TO glue_group;


--
-- Name: TABLE dtag_pool_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_pool_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_pool_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_pool_view TO author_group;


--
-- Name: TABLE poolentry; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.poolentry TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.poolentry TO author_group;
GRANT SELECT ON TABLE public.poolentry TO glue_group;


--
-- Name: TABLE dtag_poolentry_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_poolentry_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_poolentry_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_poolentry_view TO author_group;


--
-- Name: TABLE sprache; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sprache TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.sprache TO author_group;
GRANT SELECT ON TABLE public.sprache TO glue_group;


--
-- Name: TABLE dtag_sprache_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.dtag_sprache_view TO retrieval_group;
GRANT SELECT ON TABLE public.dtag_sprache_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.dtag_sprache_view TO author_group;


--
-- Name: TABLE econnections; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.econnections TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.econnections TO author_group;
GRANT SELECT ON TABLE public.econnections TO glue_group;


--
-- Name: TABLE ehotspot; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ehotspot TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ehotspot TO author_group;
GRANT SELECT ON TABLE public.ehotspot TO glue_group;


--
-- Name: TABLE eitemdata; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.eitemdata TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.eitemdata TO author_group;
GRANT SELECT ON TABLE public.eitemdata TO glue_group;


--
-- Name: TABLE eitems; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.eitems TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.eitems TO author_group;
GRANT SELECT ON TABLE public.eitems TO glue_group;


--
-- Name: TABLE elinks; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.elinks TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.elinks TO author_group;
GRANT SELECT ON TABLE public.elinks TO glue_group;


--
-- Name: TABLE emechlink; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.emechlink TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.emechlink TO author_group;
GRANT SELECT ON TABLE public.emechlink TO glue_group;


--
-- Name: TABLE enum; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.enum TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.enum TO author_group;
GRANT SELECT ON TABLE public.enum TO glue_group;


--
-- Name: TABLE enumlink; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.enumlink TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.enumlink TO author_group;
GRANT SELECT ON TABLE public.enumlink TO glue_group;


--
-- Name: TABLE epartdata; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.epartdata TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.epartdata TO author_group;
GRANT SELECT ON TABLE public.epartdata TO glue_group;


--
-- Name: TABLE epartlink; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.epartlink TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.epartlink TO author_group;
GRANT SELECT ON TABLE public.epartlink TO glue_group;


--
-- Name: TABLE eparts; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.eparts TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.eparts TO author_group;
GRANT SELECT ON TABLE public.eparts TO glue_group;


--
-- Name: TABLE eschemaentry; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.eschemaentry TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.eschemaentry TO author_group;
GRANT SELECT ON TABLE public.eschemaentry TO glue_group;


--
-- Name: TABLE eschemahead; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.eschemahead TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.eschemahead TO author_group;
GRANT SELECT ON TABLE public.eschemahead TO glue_group;


--
-- Name: TABLE esheet; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.esheet TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.esheet TO author_group;
GRANT SELECT ON TABLE public.esheet TO glue_group;


--
-- Name: TABLE estruct; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.estruct TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.estruct TO author_group;
GRANT SELECT ON TABLE public.estruct TO glue_group;


--
-- Name: TABLE etrans; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.etrans TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.etrans TO author_group;
GRANT SELECT ON TABLE public.etrans TO glue_group;


--
-- Name: TABLE etree; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.etree TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.etree TO author_group;
GRANT SELECT ON TABLE public.etree TO glue_group;


--
-- Name: TABLE export_dialog; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.export_dialog TO retrieval_group;
GRANT SELECT ON TABLE public.export_dialog TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.export_dialog TO author_group;


--
-- Name: TABLE export_ki_predict; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.export_ki_predict TO retrieval_group;
GRANT SELECT ON TABLE public.export_ki_predict TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.export_ki_predict TO author_group;


--
-- Name: TABLE export_ki_retrain; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.export_ki_retrain TO retrieval_group;
GRANT SELECT ON TABLE public.export_ki_retrain TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.export_ki_retrain TO author_group;


--
-- Name: TABLE export_lkw; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.export_lkw TO retrieval_group;
GRANT SELECT ON TABLE public.export_lkw TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.export_lkw TO author_group;


--
-- Name: TABLE export_psk_bm; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.export_psk_bm TO retrieval_group;
GRANT SELECT ON TABLE public.export_psk_bm TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.export_psk_bm TO author_group;


--
-- Name: TABLE export_psk_referenzen; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.export_psk_referenzen TO retrieval_group;
GRANT SELECT ON TABLE public.export_psk_referenzen TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.export_psk_referenzen TO author_group;


--
-- Name: TABLE favorites; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.favorites TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.favorites TO author_group;
GRANT SELECT ON TABLE public.favorites TO glue_group;


--
-- Name: TABLE flyway_schema_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.flyway_schema_history TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.flyway_schema_history TO author_group;


--
-- Name: TABLE groupentry; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.groupentry TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.groupentry TO author_group;
GRANT SELECT ON TABLE public.groupentry TO glue_group;


--
-- Name: TABLE groupfunc; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.groupfunc TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.groupfunc TO author_group;
GRANT SELECT ON TABLE public.groupfunc TO glue_group;


--
-- Name: TABLE icons; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.icons TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.icons TO author_group;
GRANT SELECT ON TABLE public.icons TO glue_group;


--
-- Name: TABLE internal_dbparams; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.internal_dbparams TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.internal_dbparams TO author_group;
GRANT SELECT ON TABLE public.internal_dbparams TO glue_group;


--
-- Name: TABLE kapitel; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.kapitel TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.kapitel TO author_group;
GRANT SELECT ON TABLE public.kapitel TO glue_group;


--
-- Name: TABLE keyvalue; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.keyvalue TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.keyvalue TO author_group;
GRANT SELECT ON TABLE public.keyvalue TO glue_group;


--
-- Name: TABLE mbag_da_code_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_code_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_code_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_code_view TO author_group;


--
-- Name: TABLE mbag_da_color_number_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_color_number_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_color_number_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_color_number_view TO author_group;


--
-- Name: TABLE mbag_da_colortable_content_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_colortable_content_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_colortable_content_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_colortable_content_view TO author_group;


--
-- Name: TABLE mbag_da_colortable_data_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_colortable_data_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_colortable_data_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_colortable_data_view TO author_group;


--
-- Name: TABLE mbag_da_colortable_factory_qft_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_colortable_factory_qft_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_colortable_factory_qft_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_colortable_factory_qft_view TO author_group;


--
-- Name: TABLE mbag_da_colortable_factory_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_colortable_factory_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_colortable_factory_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_colortable_factory_view TO author_group;


--
-- Name: TABLE mbag_da_colortable_part_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_colortable_part_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_colortable_part_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_colortable_part_view TO author_group;


--
-- Name: TABLE mbag_da_product_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_product_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_product_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_product_view TO author_group;


--
-- Name: TABLE mbag_da_module_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_module_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_module_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_module_view TO author_group;


--
-- Name: TABLE mbag_da_comb_text_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_comb_text_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_comb_text_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_comb_text_view TO author_group;


--
-- Name: TABLE mbag_da_dict_meta_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_dict_meta_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_dict_meta_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_dict_meta_view TO author_group;


--
-- Name: TABLE mbag_da_factory_data_products_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_factory_data_products_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_factory_data_products_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_factory_data_products_view TO author_group;


--
-- Name: TABLE mbag_da_factory_data_sa_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_factory_data_sa_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_factory_data_sa_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_factory_data_sa_view TO author_group;


--
-- Name: TABLE mbag_da_fn_content_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_fn_content_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_fn_content_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_fn_content_view TO author_group;


--
-- Name: TABLE mbag_da_fn_kat_ref_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_fn_kat_ref_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_fn_kat_ref_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_fn_kat_ref_view TO author_group;


--
-- Name: TABLE mbag_da_fn_mat_ref_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_fn_mat_ref_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_fn_mat_ref_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_fn_mat_ref_view TO author_group;


--
-- Name: TABLE mbag_da_fn_saa_ref_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_fn_saa_ref_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_fn_saa_ref_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_fn_saa_ref_view TO author_group;


--
-- Name: TABLE mbag_da_fn_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_fn_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_fn_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_fn_view TO author_group;


--
-- Name: TABLE mbag_da_generic_install_location_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_generic_install_location_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_generic_install_location_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_generic_install_location_view TO author_group;


--
-- Name: TABLE mbag_da_include_part_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_include_part_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_include_part_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_include_part_view TO author_group;


--
-- Name: TABLE mbag_da_kgtu_as_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_kgtu_as_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_kgtu_as_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_kgtu_as_view TO author_group;


--
-- Name: TABLE mbag_da_model_aggs_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_model_aggs_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_model_aggs_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_model_aggs_view TO author_group;


--
-- Name: TABLE mbag_da_model_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_model_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_model_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_model_view TO author_group;


--
-- Name: TABLE mbag_da_module_cemat_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_module_cemat_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_module_cemat_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_module_cemat_view TO author_group;


--
-- Name: TABLE mbag_da_modules_einpas_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_modules_einpas_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_modules_einpas_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_modules_einpas_view TO author_group;


--
-- Name: TABLE mbag_da_omitted_parts_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_omitted_parts_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_omitted_parts_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_omitted_parts_view TO author_group;


--
-- Name: TABLE mbag_da_primus_include_part_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_primus_include_part_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_primus_include_part_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_primus_include_part_view TO author_group;


--
-- Name: TABLE mbag_da_primus_replace_part_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_primus_replace_part_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_primus_replace_part_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_primus_replace_part_view TO author_group;


--
-- Name: TABLE mbag_da_product_factories_plant_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_product_factories_plant_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_product_factories_plant_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_product_factories_plant_view TO author_group;


--
-- Name: TABLE mbag_da_product_factories_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_product_factories_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_product_factories_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_product_factories_view TO author_group;


--
-- Name: TABLE mbag_da_product_models_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_product_models_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_product_models_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_product_models_view TO author_group;


--
-- Name: TABLE mbag_da_product_modules_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_product_modules_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_product_modules_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_product_modules_view TO author_group;


--
-- Name: TABLE mbag_da_product_sas_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_product_sas_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_product_sas_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_product_sas_view TO author_group;


--
-- Name: TABLE mbag_da_replace_part_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_replace_part_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_replace_part_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_replace_part_view TO author_group;


--
-- Name: TABLE mbag_da_report_const_nodes_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_report_const_nodes_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_report_const_nodes_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_report_const_nodes_view TO author_group;


--
-- Name: TABLE mbag_da_response_data_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_response_data_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_response_data_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_response_data_view TO author_group;


--
-- Name: TABLE mbag_da_response_spikes_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_response_spikes_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_response_spikes_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_response_spikes_view TO author_group;


--
-- Name: TABLE mbag_da_sa_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_sa_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_sa_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_sa_view TO author_group;


--
-- Name: TABLE mbag_da_saa_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_saa_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_saa_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_saa_view TO author_group;


--
-- Name: TABLE mbag_da_series_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_series_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_series_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_series_view TO author_group;


--
-- Name: TABLE mbag_da_top_tus_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_top_tus_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_top_tus_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_top_tus_view TO author_group;


--
-- Name: TABLE mbag_da_vin_model_mapping_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_vin_model_mapping_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_vin_model_mapping_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_vin_model_mapping_view TO author_group;


--
-- Name: TABLE mbag_da_wire_harness_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_da_wire_harness_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_da_wire_harness_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_da_wire_harness_view TO author_group;


--
-- Name: TABLE mbag_dwarray_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_dwarray_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_dwarray_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_dwarray_view TO author_group;


--
-- Name: TABLE mbag_images_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_images_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_images_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_images_view TO author_group;


--
-- Name: TABLE mbag_katalog_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_katalog_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_katalog_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_katalog_view TO author_group;


--
-- Name: TABLE mbag_links_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_links_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_links_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_links_view TO author_group;


--
-- Name: TABLE mbag_mat_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_mat_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_mat_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_mat_view TO author_group;


--
-- Name: TABLE mbag_pool_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_pool_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_pool_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_pool_view TO author_group;


--
-- Name: TABLE mbag_poolentry_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_poolentry_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_poolentry_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_poolentry_view TO author_group;


--
-- Name: TABLE mbag_sprache_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.mbag_sprache_view TO retrieval_group;
GRANT SELECT ON TABLE public.mbag_sprache_view TO glue_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.mbag_sprache_view TO author_group;


--
-- Name: TABLE notiz; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.notiz TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.notiz TO author_group;
GRANT SELECT ON TABLE public.notiz TO glue_group;


--
-- Name: TABLE preise; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.preise TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.preise TO author_group;
GRANT SELECT ON TABLE public.preise TO glue_group;


--
-- Name: TABLE s_chain; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.s_chain TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.s_chain TO author_group;
GRANT SELECT ON TABLE public.s_chain TO glue_group;


--
-- Name: TABLE s_items; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.s_items TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.s_items TO author_group;
GRANT SELECT ON TABLE public.s_items TO glue_group;


--
-- Name: TABLE s_set; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.s_set TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.s_set TO author_group;
GRANT SELECT ON TABLE public.s_set TO glue_group;


--
-- Name: TABLE sbadr; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sbadr TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.sbadr TO author_group;
GRANT SELECT ON TABLE public.sbadr TO glue_group;


--
-- Name: TABLE sbdetail; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sbdetail TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.sbdetail TO author_group;
GRANT SELECT ON TABLE public.sbdetail TO glue_group;


--
-- Name: TABLE strukt; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.strukt TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.strukt TO author_group;
GRANT SELECT ON TABLE public.strukt TO glue_group;


--
-- Name: TABLE treeid; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.treeid TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.treeid TO author_group;
GRANT SELECT ON TABLE public.treeid TO glue_group;


--
-- Name: TABLE treemod; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.treemod TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.treemod TO author_group;
GRANT SELECT ON TABLE public.treemod TO glue_group;


--
-- Name: TABLE u_serno; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.u_serno TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.u_serno TO author_group;
GRANT SELECT ON TABLE public.u_serno TO glue_group;


--
-- Name: TABLE ua_apps; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_apps TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_apps TO author_group;
GRANT SELECT ON TABLE public.ua_apps TO glue_group;


--
-- Name: TABLE ua_news; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_news TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_news TO author_group;
GRANT SELECT ON TABLE public.ua_news TO glue_group;


--
-- Name: TABLE ua_news_feedback; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_news_feedback TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_news_feedback TO author_group;
GRANT SELECT ON TABLE public.ua_news_feedback TO glue_group;


--
-- Name: TABLE ua_news_texts; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_news_texts TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_news_texts TO author_group;
GRANT SELECT ON TABLE public.ua_news_texts TO glue_group;


--
-- Name: TABLE ua_organisation_apps; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_organisation_apps TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_organisation_apps TO author_group;
GRANT SELECT ON TABLE public.ua_organisation_apps TO glue_group;


--
-- Name: TABLE ua_organisation_properties; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_organisation_properties TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_organisation_properties TO author_group;
GRANT SELECT ON TABLE public.ua_organisation_properties TO glue_group;


--
-- Name: TABLE ua_organisation_roles; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_organisation_roles TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_organisation_roles TO author_group;
GRANT SELECT ON TABLE public.ua_organisation_roles TO glue_group;


--
-- Name: TABLE ua_organisations; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_organisations TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_organisations TO author_group;
GRANT SELECT ON TABLE public.ua_organisations TO glue_group;


--
-- Name: TABLE ua_rights; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_rights TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_rights TO author_group;
GRANT SELECT ON TABLE public.ua_rights TO glue_group;


--
-- Name: TABLE ua_role_rights; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_role_rights TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_role_rights TO author_group;
GRANT SELECT ON TABLE public.ua_role_rights TO glue_group;


--
-- Name: TABLE ua_roles; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_roles TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_roles TO author_group;
GRANT SELECT ON TABLE public.ua_roles TO glue_group;


--
-- Name: TABLE ua_user_admin_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_user_admin_history TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_user_admin_history TO author_group;
GRANT SELECT ON TABLE public.ua_user_admin_history TO glue_group;


--
-- Name: TABLE ua_user_data_templates; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_user_data_templates TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_user_data_templates TO author_group;
GRANT SELECT ON TABLE public.ua_user_data_templates TO glue_group;


--
-- Name: TABLE ua_user_organisations; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_user_organisations TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_user_organisations TO author_group;
GRANT SELECT ON TABLE public.ua_user_organisations TO glue_group;


--
-- Name: TABLE ua_user_prop_templates; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_user_prop_templates TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_user_prop_templates TO author_group;
GRANT SELECT ON TABLE public.ua_user_prop_templates TO glue_group;


--
-- Name: TABLE ua_user_properties; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_user_properties TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_user_properties TO author_group;
GRANT SELECT ON TABLE public.ua_user_properties TO glue_group;


--
-- Name: TABLE ua_user_roles; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_user_roles TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_user_roles TO author_group;
GRANT SELECT ON TABLE public.ua_user_roles TO glue_group;


--
-- Name: TABLE ua_users; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.ua_users TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.ua_users TO author_group;
GRANT SELECT ON TABLE public.ua_users TO glue_group;


--
-- Name: TABLE usergroup; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.usergroup TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.usergroup TO author_group;
GRANT SELECT ON TABLE public.usergroup TO glue_group;


--
-- Name: TABLE usersettings; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.usersettings TO retrieval_group;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.usersettings TO author_group;
GRANT SELECT ON TABLE public.usersettings TO glue_group;


--
-- PostgreSQL database dump complete
--

