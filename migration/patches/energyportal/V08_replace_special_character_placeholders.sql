CREATE OR REPLACE FUNCTION fcs.replace_special_characters(p_text TEXT) RETURNS TEXT
    LANGUAGE 'plpgsql'
AS $$
DECLARE
    l_clean_text TEXT := p_text;
    l_marker_start_text TEXT := '[_CHR';
    l_marker_end_text TEXT := '_]';
BEGIN
    -- the following are unused in Windows-1252 so replace here with an empty string if found
    l_clean_text := replace(l_clean_text, l_marker_start_text||129||l_marker_end_text, '');
    l_clean_text := replace(l_clean_text, l_marker_start_text||141||l_marker_end_text, '');
    l_clean_text := replace(l_clean_text, l_marker_start_text||143||l_marker_end_text, '');
    l_clean_text := replace(l_clean_text, l_marker_start_text||144||l_marker_end_text, '');
    l_clean_text := replace(l_clean_text, l_marker_start_text||157||l_marker_end_text, '');

    -- these are special cases where the Windows-1252 decimal character code doesn't match that in UTF-8
    -- see here for further details https://www.ascii-code.com/
    l_clean_text := replace(l_clean_text, l_marker_start_text||128||l_marker_end_text, CHR(8364));
    l_clean_text := replace(l_clean_text, l_marker_start_text||130||l_marker_end_text, CHR(8218));
    l_clean_text := replace(l_clean_text, l_marker_start_text||131||l_marker_end_text, CHR(402));
    l_clean_text := replace(l_clean_text, l_marker_start_text||132||l_marker_end_text, CHR(8222));
    l_clean_text := replace(l_clean_text, l_marker_start_text||133||l_marker_end_text, CHR(8230));
    l_clean_text := replace(l_clean_text, l_marker_start_text||134||l_marker_end_text, CHR(8224));
    l_clean_text := replace(l_clean_text, l_marker_start_text||135||l_marker_end_text, CHR(8225));
    l_clean_text := replace(l_clean_text, l_marker_start_text||136||l_marker_end_text, CHR(710));
    l_clean_text := replace(l_clean_text, l_marker_start_text||137||l_marker_end_text, CHR(8240));
    l_clean_text := replace(l_clean_text, l_marker_start_text||138||l_marker_end_text, CHR(352));
    l_clean_text := replace(l_clean_text, l_marker_start_text||139||l_marker_end_text, CHR(8249));
    l_clean_text := replace(l_clean_text, l_marker_start_text||140||l_marker_end_text, CHR(338));
    l_clean_text := replace(l_clean_text, l_marker_start_text||142||l_marker_end_text, CHR(381));
    l_clean_text := replace(l_clean_text, l_marker_start_text||145||l_marker_end_text, CHR(8216));
    l_clean_text := replace(l_clean_text, l_marker_start_text||146||l_marker_end_text, CHR(8217));
    l_clean_text := replace(l_clean_text, l_marker_start_text||147||l_marker_end_text, CHR(8220));
    l_clean_text := replace(l_clean_text, l_marker_start_text||148||l_marker_end_text, CHR(8221));
    l_clean_text := replace(l_clean_text, l_marker_start_text||149||l_marker_end_text, CHR(8226));
    l_clean_text := replace(l_clean_text, l_marker_start_text||150||l_marker_end_text, CHR(8211));
    l_clean_text := replace(l_clean_text, l_marker_start_text||151||l_marker_end_text, CHR(8212));
    l_clean_text := replace(l_clean_text, l_marker_start_text||152||l_marker_end_text, CHR(732));
    l_clean_text := replace(l_clean_text, l_marker_start_text||153||l_marker_end_text, CHR(8482));
    l_clean_text := replace(l_clean_text, l_marker_start_text||154||l_marker_end_text, CHR(353));
    l_clean_text := replace(l_clean_text, l_marker_start_text||155||l_marker_end_text, CHR(8250));
    l_clean_text := replace(l_clean_text, l_marker_start_text||156||l_marker_end_text, CHR(339));
    l_clean_text := replace(l_clean_text, l_marker_start_text||158||l_marker_end_text, CHR(382));
    l_clean_text := replace(l_clean_text, l_marker_start_text||159||l_marker_end_text, CHR(376));

    -- the decimal character codes in Windows-1252 match UTF-8 for the remaining extended ASCII character codes
    FOR i IN 160..255 LOOP
        l_clean_text := replace(l_clean_text, l_marker_start_text||i||l_marker_end_text, CHR(i));
    END LOOP;

    RETURN l_clean_text;
END;
$$;

-- SELECT *, fcs.replace_special_characters(notes)
-- FROM fcs.application_supporting_information
-- WHERE notes LIKE '%[_CHR%_]%';

UPDATE fcs.application_supporting_information
SET notes = fcs.replace_special_characters(notes)
WHERE notes LIKE '%[_CHR%_]%';

-- SELECT *, fcs.replace_special_characters(erap_notes)
-- FROM fcs.application_supporting_information
-- WHERE erap_notes LIKE '%[_CHR%_]%';

UPDATE fcs.application_supporting_information
SET erap_notes = fcs.replace_special_characters(erap_notes)
WHERE erap_notes LIKE '%[_CHR%_]%';

-- SELECT *, fcs.replace_special_characters(case_note_text)
-- FROM fcs.application_case_notes
-- WHERE case_note_text LIKE '%[_CHR%_]%';

UPDATE fcs.application_case_notes
SET case_note_text = fcs.replace_special_characters(case_note_text)
WHERE case_note_text LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.application_technical_reviews
-- WHERE response_text LIKE '%[_CHR%_]%';

UPDATE fcs.application_technical_reviews
SET response_text = fcs.replace_special_characters(response_text)
WHERE response_text LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.application_updates
-- WHERE request_text LIKE '%[_CHR%_]%';

UPDATE fcs.application_updates
SET request_text = fcs.replace_special_characters(request_text)
WHERE request_text LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.file_upload_library_uploaded_files
-- WHERE description LIKE '%[_CHR%_]%';

UPDATE fcs.file_upload_library_uploaded_files
SET description = fcs.replace_special_characters(description)
WHERE description LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.application_other_legacy_data
-- WHERE es_reference LIKE '%[_CHR%_]%';

UPDATE fcs.application_other_legacy_data
SET es_reference = fcs.replace_special_characters(es_reference)
WHERE es_reference LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.application_other_legacy_data
-- WHERE terminal_name LIKE '%[_CHR%_]%';

UPDATE fcs.application_other_legacy_data
SET terminal_name = fcs.replace_special_characters(terminal_name)
WHERE terminal_name LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.application_other_legacy_data
-- WHERE terminal_location LIKE '%[_CHR%_]%';

UPDATE fcs.application_other_legacy_data
SET terminal_location = fcs.replace_special_characters(terminal_location)
WHERE terminal_location LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.flares
-- WHERE description LIKE '%[_CHR%_]%';

UPDATE fcs.flares
SET description = fcs.replace_special_characters(description)
WHERE description LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.flares
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.flares
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.flare_annual_months
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.flare_annual_months
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.flare_annual_123_months
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.flare_annual_123_months
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.flare_short_term_months
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.flare_short_term_months
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.flare_short_term_123_months
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.flare_short_term_123_months
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.flare_report_months
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.flare_report_months
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.vents
-- WHERE description LIKE '%[_CHR%_]%';

UPDATE fcs.vents
SET description = fcs.replace_special_characters(description)
WHERE description LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.vents
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.vents
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.vent_annual_months
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.vent_annual_months
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.vent_annual_123_months
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.vent_annual_123_months
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.vent_short_term_months
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.vent_short_term_months
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.vent_short_term_123_months
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.vent_short_term_123_months
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

-- SELECT *
-- FROM fcs.vent_report_months
-- WHERE comments LIKE '%[_CHR%_]%';

UPDATE fcs.vent_report_months
SET comments = fcs.replace_special_characters(comments)
WHERE comments LIKE '%[_CHR%_]%';

DROP FUNCTION fcs.replace_special_characters(TEXT);
