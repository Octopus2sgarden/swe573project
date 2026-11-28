-- V4: seed the ontology vocabularies from the wiki page "Craft Ontology" (#55).
--
-- 268 terms: CATEGORY 58, MATERIAL 56, TECHNIQUE 54, TOOL 23, COLOR 15, REGION 45, PERIOD 17.
-- Slugs are prefixed with the vocabulary (cat-, mat-, tech-, tool-, color-, region-, period-)
-- so the same word can exist in two vocabularies (e.g. cat-knitting and tech-knitting).
-- Wikidata IDs are left empty on purpose: the wiki says they must be checked, never guessed.
-- Period years are approximate; negative years are BCE.

-- Helper that exists only during this migration (pg_temp = this session's temporary schema).
-- It looks up the parent by slug, checks it is in the same vocabulary, and puts the new term
-- after its existing siblings (sort_order = number of siblings + 1).
CREATE FUNCTION pg_temp.term(
    p_vocab  TEXT,
    p_slug   TEXT,
    p_label  TEXT,
    parent   TEXT DEFAULT NULL,
    local    TEXT DEFAULT NULL,
    alt      TEXT DEFAULT NULL,
    def      TEXT DEFAULT NULL,
    hex      TEXT DEFAULT NULL,
    y_from   INT  DEFAULT NULL,
    y_to     INT  DEFAULT NULL
) RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE
    v_parent_id BIGINT;
    v_parent_vocab TEXT;
BEGIN
    IF parent IS NOT NULL THEN
        SELECT id, vocabulary INTO v_parent_id, v_parent_vocab FROM ontology_term WHERE slug = parent;
        IF v_parent_id IS NULL THEN
            RAISE EXCEPTION 'Parent term % not found (for %)', parent, p_slug;
        END IF;
        IF v_parent_vocab <> p_vocab THEN
            RAISE EXCEPTION 'Parent % is in vocabulary %, not %', parent, v_parent_vocab, p_vocab;
        END IF;
    END IF;

    INSERT INTO ontology_term (vocabulary, slug, label, local_name, alt_labels, definition,
                               parent_id, color_hex, year_from, year_to, sort_order)
    VALUES (p_vocab, p_slug, p_label, local, alt, def, v_parent_id, hex, y_from, y_to,
            (SELECT count(*) + 1 FROM ontology_term
              WHERE vocabulary = p_vocab AND parent_id IS NOT DISTINCT FROM v_parent_id));
END;
$$;

-- ----------------------------------------------------------------------
-- Categories: what is it?
-- ----------------------------------------------------------------------
SELECT pg_temp.term('CATEGORY', 'cat-ceramics', 'Ceramics', def => 'Objects shaped from clay and hardened by firing.');
  SELECT pg_temp.term('CATEGORY', 'cat-pottery', 'Pottery', parent => 'cat-ceramics', local => 'Çömlekçilik');
    SELECT pg_temp.term('CATEGORY', 'cat-avanos-pottery', 'Avanos pottery', parent => 'cat-pottery', local => 'Avanos çömlekçiliği', def => 'Red-clay pottery from Avanos in Cappadocia, made from Kızılırmak river clay.');
  SELECT pg_temp.term('CATEGORY', 'cat-cini', 'Çini', parent => 'cat-ceramics', local => 'Çini', alt => 'glazed tiles; glazed ware', def => 'Glazed tiles and ware with underglaze painting on a quartz-rich body.');
    SELECT pg_temp.term('CATEGORY', 'cat-kutahya-cinisi', 'Kütahya çinisi', parent => 'cat-cini', local => 'Kütahya çinisi', def => 'Çini from Kütahya, known for free brushwork and a wide palette.');
    SELECT pg_temp.term('CATEGORY', 'cat-iznik-cinisi', 'İznik çinisi', parent => 'cat-cini', local => 'İznik çinisi', def => 'Çini from İznik, known for its quartz body, cobalt blue and coral red.');
  SELECT pg_temp.term('CATEGORY', 'cat-terracotta', 'Terracotta', parent => 'cat-ceramics', def => 'Unglazed, low-fired earthenware.');
SELECT pg_temp.term('CATEGORY', 'cat-textiles', 'Textiles', def => 'Objects made from fibers by weaving, knotting, stitching or felting.');
  SELECT pg_temp.term('CATEGORY', 'cat-carpets-and-rugs', 'Carpets and rugs', parent => 'cat-textiles', local => 'Halı');
    SELECT pg_temp.term('CATEGORY', 'cat-usak-halisi', 'Uşak halısı', parent => 'cat-carpets-and-rugs', local => 'Uşak halısı', def => 'Knotted wool carpets from Uşak.');
    SELECT pg_temp.term('CATEGORY', 'cat-hereke-halisi', 'Hereke halısı', parent => 'cat-carpets-and-rugs', local => 'Hereke halısı', def => 'Fine silk and wool carpets from Hereke.');
  SELECT pg_temp.term('CATEGORY', 'cat-kilim', 'Kilim', parent => 'cat-textiles', local => 'Kilim', alt => 'flat-woven rug', def => 'Flat-woven rug made with the slit tapestry technique.');
  SELECT pg_temp.term('CATEGORY', 'cat-woven-cloth-and-scarves', 'Woven cloth and scarves', parent => 'cat-textiles', local => 'Dokuma');
  SELECT pg_temp.term('CATEGORY', 'cat-embroidery', 'Embroidery', parent => 'cat-textiles', local => 'Nakış');
  SELECT pg_temp.term('CATEGORY', 'cat-needle-lace', 'Needle lace', parent => 'cat-textiles', local => 'Oya', def => 'Lace edging made with a needle, often in flower shapes.');
  SELECT pg_temp.term('CATEGORY', 'cat-felt', 'Felt', parent => 'cat-textiles', local => 'Keçe', def => 'Fabric made by matting wool fibers with water, soap and pressure.');
  SELECT pg_temp.term('CATEGORY', 'cat-knitting', 'Knitting', parent => 'cat-textiles', local => 'Örgü');
  SELECT pg_temp.term('CATEGORY', 'cat-printed-textiles', 'Printed textiles', parent => 'cat-textiles', local => 'Yazma, baskı');
    SELECT pg_temp.term('CATEGORY', 'cat-tokat-yazmasi', 'Tokat yazması', parent => 'cat-printed-textiles', local => 'Tokat yazması', def => 'Block-printed cotton cloth from Tokat.');
SELECT pg_temp.term('CATEGORY', 'cat-woodwork', 'Woodwork', def => 'Objects carved, joined or turned from wood.');
  SELECT pg_temp.term('CATEGORY', 'cat-wood-carving', 'Wood carving', parent => 'cat-woodwork', local => 'Ahşap oymacılığı');
  SELECT pg_temp.term('CATEGORY', 'cat-inlay', 'Inlay', parent => 'cat-woodwork', local => 'Kakma');
    SELECT pg_temp.term('CATEGORY', 'cat-mother-of-pearl-inlay', 'Mother-of-pearl inlay', parent => 'cat-inlay', local => 'Sedef kakma', def => 'Wood inlaid with pieces of mother-of-pearl.');
  SELECT pg_temp.term('CATEGORY', 'cat-interlocking-joinery', 'Interlocking joinery', parent => 'cat-woodwork', local => 'Kündekari', def => 'Wooden panels assembled from small pieces fitted together without nails or glue.');
  SELECT pg_temp.term('CATEGORY', 'cat-woodturning', 'Woodturning', parent => 'cat-woodwork', local => 'Ahşap tornacılığı');
SELECT pg_temp.term('CATEGORY', 'cat-metalwork', 'Metalwork', def => 'Objects shaped from metal by hammering, casting or wirework.');
  SELECT pg_temp.term('CATEGORY', 'cat-copperware', 'Copperware', parent => 'cat-metalwork', local => 'Bakırcılık');
  SELECT pg_temp.term('CATEGORY', 'cat-silver-filigree', 'Silver filigree', parent => 'cat-metalwork', local => 'Telkari', def => 'Jewelry and objects made from fine twisted silver wire.');
    SELECT pg_temp.term('CATEGORY', 'cat-mardin-telkarisi', 'Mardin telkarisi', parent => 'cat-silver-filigree', local => 'Mardin telkarisi', def => 'Silver filigree from Mardin and Midyat.');
  SELECT pg_temp.term('CATEGORY', 'cat-blacksmithing', 'Blacksmithing', parent => 'cat-metalwork', local => 'Demircilik');
  SELECT pg_temp.term('CATEGORY', 'cat-engraved-metal', 'Engraved metal', parent => 'cat-metalwork', local => 'Kalemkârlık');
SELECT pg_temp.term('CATEGORY', 'cat-jewelry-and-beadwork', 'Jewelry and Beadwork');
  SELECT pg_temp.term('CATEGORY', 'cat-beadwork', 'Beadwork', parent => 'cat-jewelry-and-beadwork', local => 'Boncuk işi');
  SELECT pg_temp.term('CATEGORY', 'cat-stone-jewelry', 'Stone jewelry', parent => 'cat-jewelry-and-beadwork');
    SELECT pg_temp.term('CATEGORY', 'cat-oltu-stone-jewelry', 'Oltu stone jewelry', parent => 'cat-stone-jewelry', local => 'Oltu taşı takıları', def => 'Jewelry and prayer beads made from black Oltu stone (jet) from Erzurum.');
SELECT pg_temp.term('CATEGORY', 'cat-glass', 'Glass');
  SELECT pg_temp.term('CATEGORY', 'cat-glassblowing', 'Glassblowing', parent => 'cat-glass', local => 'Cam üfleme');
  SELECT pg_temp.term('CATEGORY', 'cat-glass-beads', 'Glass beads', parent => 'cat-glass', local => 'Cam boncuk');
    SELECT pg_temp.term('CATEGORY', 'cat-evil-eye-bead', 'Evil eye bead', parent => 'cat-glass-beads', local => 'Nazar boncuğu', def => 'Blue glass bead with an eye motif, believed to protect against the evil eye.');
SELECT pg_temp.term('CATEGORY', 'cat-basketry-and-plant-fiber', 'Basketry and Plant Fiber');
  SELECT pg_temp.term('CATEGORY', 'cat-basket-weaving', 'Basket weaving', parent => 'cat-basketry-and-plant-fiber', local => 'Sepetçilik');
  SELECT pg_temp.term('CATEGORY', 'cat-mat-weaving', 'Mat weaving', parent => 'cat-basketry-and-plant-fiber', local => 'Hasır örücülüğü');
SELECT pg_temp.term('CATEGORY', 'cat-leatherwork', 'Leatherwork');
  SELECT pg_temp.term('CATEGORY', 'cat-leather-goods', 'Leather goods', parent => 'cat-leatherwork', local => 'Deri işçiliği');
  SELECT pg_temp.term('CATEGORY', 'cat-traditional-footwear', 'Traditional footwear', parent => 'cat-leatherwork', local => 'Yemeni, çarık');
  SELECT pg_temp.term('CATEGORY', 'cat-saddlery', 'Saddlery', parent => 'cat-leatherwork', local => 'Saraciye');
SELECT pg_temp.term('CATEGORY', 'cat-paper-and-book-arts', 'Paper and Book Arts');
  SELECT pg_temp.term('CATEGORY', 'cat-paper-marbling', 'Paper marbling', parent => 'cat-paper-and-book-arts', local => 'Ebru', def => 'Patterns made by floating paint on thickened water and transferring them to paper.');
  SELECT pg_temp.term('CATEGORY', 'cat-calligraphy', 'Calligraphy', parent => 'cat-paper-and-book-arts', local => 'Hat', def => 'The art of beautiful writing, especially in Arabic script.');
  SELECT pg_temp.term('CATEGORY', 'cat-illumination', 'Illumination', parent => 'cat-paper-and-book-arts', local => 'Tezhip', def => 'Decorating manuscripts and calligraphy with gold and painted ornament.');
  SELECT pg_temp.term('CATEGORY', 'cat-miniature-painting', 'Miniature painting', parent => 'cat-paper-and-book-arts', local => 'Minyatür');
SELECT pg_temp.term('CATEGORY', 'cat-stonework', 'Stonework');
  SELECT pg_temp.term('CATEGORY', 'cat-stone-carving', 'Stone carving', parent => 'cat-stonework', local => 'Taş oymacılığı');
SELECT pg_temp.term('CATEGORY', 'cat-musical-instruments', 'Musical Instruments');
  SELECT pg_temp.term('CATEGORY', 'cat-string-instruments', 'String instruments', parent => 'cat-musical-instruments', local => 'Telli çalgılar');
    SELECT pg_temp.term('CATEGORY', 'cat-baglama', 'Bağlama', parent => 'cat-string-instruments', local => 'Bağlama', alt => 'saz', def => 'Long-necked lute of Anatolian folk music.');
  SELECT pg_temp.term('CATEGORY', 'cat-wind-instruments', 'Wind instruments', parent => 'cat-musical-instruments', local => 'Nefesli çalgılar');
  SELECT pg_temp.term('CATEGORY', 'cat-percussion-instruments', 'Percussion instruments', parent => 'cat-musical-instruments', local => 'Vurmalı çalgılar');

-- ----------------------------------------------------------------------
-- Materials: what is it made of?
-- ----------------------------------------------------------------------
SELECT pg_temp.term('MATERIAL', 'mat-plant-fibers', 'Plant fibers');
  SELECT pg_temp.term('MATERIAL', 'mat-cotton', 'Cotton', parent => 'mat-plant-fibers', local => 'Pamuk');
  SELECT pg_temp.term('MATERIAL', 'mat-linen', 'Linen', parent => 'mat-plant-fibers', local => 'Keten', alt => 'flax');
  SELECT pg_temp.term('MATERIAL', 'mat-hemp', 'Hemp', parent => 'mat-plant-fibers', local => 'Kenevir');
  SELECT pg_temp.term('MATERIAL', 'mat-jute', 'Jute', parent => 'mat-plant-fibers', local => 'Jüt');
  SELECT pg_temp.term('MATERIAL', 'mat-reed-and-rush', 'Reed and rush', parent => 'mat-plant-fibers', local => 'Kamış, saz');
  SELECT pg_temp.term('MATERIAL', 'mat-willow', 'Willow', parent => 'mat-plant-fibers', local => 'Söğüt');
  SELECT pg_temp.term('MATERIAL', 'mat-bamboo', 'Bamboo', parent => 'mat-plant-fibers', local => 'Bambu');
SELECT pg_temp.term('MATERIAL', 'mat-animal-fibers', 'Animal fibers');
  SELECT pg_temp.term('MATERIAL', 'mat-sheep-wool', 'Sheep wool', parent => 'mat-animal-fibers', local => 'Yün');
  SELECT pg_temp.term('MATERIAL', 'mat-goat-hair', 'Goat hair', parent => 'mat-animal-fibers', local => 'Keçi kılı', alt => 'mohair; tiftik');
  SELECT pg_temp.term('MATERIAL', 'mat-silk', 'Silk', parent => 'mat-animal-fibers', local => 'İpek');
  SELECT pg_temp.term('MATERIAL', 'mat-camel-hair', 'Camel hair', parent => 'mat-animal-fibers', local => 'Deve yünü');
SELECT pg_temp.term('MATERIAL', 'mat-other-animal-materials', 'Other animal materials');
  SELECT pg_temp.term('MATERIAL', 'mat-leather', 'Leather', parent => 'mat-other-animal-materials', local => 'Deri');
  SELECT pg_temp.term('MATERIAL', 'mat-bone', 'Bone', parent => 'mat-other-animal-materials', local => 'Kemik');
  SELECT pg_temp.term('MATERIAL', 'mat-horn', 'Horn', parent => 'mat-other-animal-materials', local => 'Boynuz');
  SELECT pg_temp.term('MATERIAL', 'mat-mother-of-pearl', 'Mother-of-pearl', parent => 'mat-other-animal-materials', local => 'Sedef');
SELECT pg_temp.term('MATERIAL', 'mat-wood', 'Wood');
  SELECT pg_temp.term('MATERIAL', 'mat-walnut', 'Walnut', parent => 'mat-wood', local => 'Ceviz');
  SELECT pg_temp.term('MATERIAL', 'mat-boxwood', 'Boxwood', parent => 'mat-wood', local => 'Şimşir');
  SELECT pg_temp.term('MATERIAL', 'mat-oak', 'Oak', parent => 'mat-wood', local => 'Meşe');
  SELECT pg_temp.term('MATERIAL', 'mat-pine', 'Pine', parent => 'mat-wood', local => 'Çam');
  SELECT pg_temp.term('MATERIAL', 'mat-cedar', 'Cedar', parent => 'mat-wood', local => 'Sedir');
  SELECT pg_temp.term('MATERIAL', 'mat-mulberry', 'Mulberry', parent => 'mat-wood', local => 'Dut');
SELECT pg_temp.term('MATERIAL', 'mat-clay-and-ceramic-materials', 'Clay and ceramic materials');
  SELECT pg_temp.term('MATERIAL', 'mat-earthenware-clay', 'Earthenware clay', parent => 'mat-clay-and-ceramic-materials', local => 'Kil');
  SELECT pg_temp.term('MATERIAL', 'mat-kaolin', 'Kaolin', parent => 'mat-clay-and-ceramic-materials', local => 'Kaolin');
  SELECT pg_temp.term('MATERIAL', 'mat-quartz', 'Quartz', parent => 'mat-clay-and-ceramic-materials', local => 'Kuvars', alt => 'frit');
  SELECT pg_temp.term('MATERIAL', 'mat-glaze', 'Glaze', parent => 'mat-clay-and-ceramic-materials', local => 'Sır');
SELECT pg_temp.term('MATERIAL', 'mat-metals', 'Metals');
  SELECT pg_temp.term('MATERIAL', 'mat-copper', 'Copper', parent => 'mat-metals', local => 'Bakır');
  SELECT pg_temp.term('MATERIAL', 'mat-silver', 'Silver', parent => 'mat-metals', local => 'Gümüş');
  SELECT pg_temp.term('MATERIAL', 'mat-gold', 'Gold', parent => 'mat-metals', local => 'Altın');
  SELECT pg_temp.term('MATERIAL', 'mat-brass', 'Brass', parent => 'mat-metals', local => 'Pirinç');
  SELECT pg_temp.term('MATERIAL', 'mat-iron', 'Iron', parent => 'mat-metals', local => 'Demir');
  SELECT pg_temp.term('MATERIAL', 'mat-tin', 'Tin', parent => 'mat-metals', local => 'Kalay');
SELECT pg_temp.term('MATERIAL', 'mat-stone-and-minerals', 'Stone and minerals');
  SELECT pg_temp.term('MATERIAL', 'mat-marble', 'Marble', parent => 'mat-stone-and-minerals', local => 'Mermer');
  SELECT pg_temp.term('MATERIAL', 'mat-oltu-stone', 'Oltu stone', parent => 'mat-stone-and-minerals', local => 'Oltu taşı', alt => 'jet');
  SELECT pg_temp.term('MATERIAL', 'mat-glass', 'Glass', parent => 'mat-stone-and-minerals', local => 'Cam');
SELECT pg_temp.term('MATERIAL', 'mat-dyes-and-pigments', 'Dyes and pigments');
  SELECT pg_temp.term('MATERIAL', 'mat-natural-plant-dyes', 'Natural plant dyes', parent => 'mat-dyes-and-pigments', local => 'Doğal boyalar');
    SELECT pg_temp.term('MATERIAL', 'mat-madder', 'Madder', parent => 'mat-natural-plant-dyes', local => 'Kökboya');
    SELECT pg_temp.term('MATERIAL', 'mat-indigo', 'Indigo', parent => 'mat-natural-plant-dyes', local => 'Çivit');
    SELECT pg_temp.term('MATERIAL', 'mat-weld', 'Weld', parent => 'mat-natural-plant-dyes', local => 'Muhabbet çiçeği');
    SELECT pg_temp.term('MATERIAL', 'mat-walnut-husk', 'Walnut husk', parent => 'mat-natural-plant-dyes', local => 'Ceviz kabuğu');
    SELECT pg_temp.term('MATERIAL', 'mat-pomegranate-rind', 'Pomegranate rind', parent => 'mat-natural-plant-dyes', local => 'Nar kabuğu');
  SELECT pg_temp.term('MATERIAL', 'mat-mineral-pigments', 'Mineral pigments', parent => 'mat-dyes-and-pigments');
    SELECT pg_temp.term('MATERIAL', 'mat-cobalt-oxide', 'Cobalt oxide', parent => 'mat-mineral-pigments', local => 'Kobalt oksit');
    SELECT pg_temp.term('MATERIAL', 'mat-iron-oxide', 'Iron oxide', parent => 'mat-mineral-pigments', local => 'Demir oksit');
  SELECT pg_temp.term('MATERIAL', 'mat-synthetic-dyes', 'Synthetic dyes', parent => 'mat-dyes-and-pigments', local => 'Sentetik boyalar');
SELECT pg_temp.term('MATERIAL', 'mat-paper-and-finishing', 'Paper and finishing');
  SELECT pg_temp.term('MATERIAL', 'mat-paper', 'Paper', parent => 'mat-paper-and-finishing', local => 'Kâğıt');
  SELECT pg_temp.term('MATERIAL', 'mat-gold-leaf', 'Gold leaf', parent => 'mat-paper-and-finishing', local => 'Varak altın');
  SELECT pg_temp.term('MATERIAL', 'mat-ink', 'Ink', parent => 'mat-paper-and-finishing', local => 'Mürekkep');

-- ----------------------------------------------------------------------
-- Techniques: how is it made?
-- ----------------------------------------------------------------------
SELECT pg_temp.term('TECHNIQUE', 'tech-ceramic-forming', 'Ceramic forming');
  SELECT pg_temp.term('TECHNIQUE', 'tech-wheel-throwing', 'Wheel throwing', parent => 'tech-ceramic-forming', local => 'Çarkta şekillendirme');
  SELECT pg_temp.term('TECHNIQUE', 'tech-hand-building', 'Hand building', parent => 'tech-ceramic-forming', local => 'Elde şekillendirme');
    SELECT pg_temp.term('TECHNIQUE', 'tech-coil-building', 'Coil building', parent => 'tech-hand-building');
    SELECT pg_temp.term('TECHNIQUE', 'tech-pinching', 'Pinching', parent => 'tech-hand-building');
    SELECT pg_temp.term('TECHNIQUE', 'tech-slab-building', 'Slab building', parent => 'tech-hand-building');
  SELECT pg_temp.term('TECHNIQUE', 'tech-moulding', 'Moulding', parent => 'tech-ceramic-forming', local => 'Kalıplama');
SELECT pg_temp.term('TECHNIQUE', 'tech-ceramic-decoration-and-firing', 'Ceramic decoration and firing');
  SELECT pg_temp.term('TECHNIQUE', 'tech-underglaze-painting', 'Underglaze painting', parent => 'tech-ceramic-decoration-and-firing', local => 'Sıraltı boyama');
  SELECT pg_temp.term('TECHNIQUE', 'tech-glazing', 'Glazing', parent => 'tech-ceramic-decoration-and-firing', local => 'Sırlama');
  SELECT pg_temp.term('TECHNIQUE', 'tech-kiln-firing', 'Kiln firing', parent => 'tech-ceramic-decoration-and-firing', local => 'Fırınlama');
SELECT pg_temp.term('TECHNIQUE', 'tech-fiber-preparation', 'Fiber preparation');
  SELECT pg_temp.term('TECHNIQUE', 'tech-carding', 'Carding', parent => 'tech-fiber-preparation', local => 'Tarama, hallaçlama');
  SELECT pg_temp.term('TECHNIQUE', 'tech-hand-spinning', 'Hand spinning', parent => 'tech-fiber-preparation', local => 'İğ ile eğirme', alt => 'spindle spinning');
SELECT pg_temp.term('TECHNIQUE', 'tech-weaving', 'Weaving');
  SELECT pg_temp.term('TECHNIQUE', 'tech-knotted-pile', 'Knotted pile', parent => 'tech-weaving', local => 'Düğümlü dokuma');
    SELECT pg_temp.term('TECHNIQUE', 'tech-symmetric-knot', 'Symmetric knot', parent => 'tech-knotted-pile', local => 'Türk düğümü', alt => 'Turkish knot; Gördes knot');
    SELECT pg_temp.term('TECHNIQUE', 'tech-asymmetric-knot', 'Asymmetric knot', parent => 'tech-knotted-pile', local => 'İran düğümü', alt => 'Persian knot; Senneh knot');
  SELECT pg_temp.term('TECHNIQUE', 'tech-slit-tapestry', 'Slit tapestry', parent => 'tech-weaving', local => 'Kilim tekniği', alt => 'kilim weave');
  SELECT pg_temp.term('TECHNIQUE', 'tech-plain-weave', 'Plain weave', parent => 'tech-weaving', local => 'Bez ayağı');
  SELECT pg_temp.term('TECHNIQUE', 'tech-twill-weave', 'Twill weave', parent => 'tech-weaving', local => 'Dimi');
SELECT pg_temp.term('TECHNIQUE', 'tech-needlework', 'Needlework');
  SELECT pg_temp.term('TECHNIQUE', 'tech-embroidery-stitching', 'Embroidery stitching', parent => 'tech-needlework', local => 'Nakış işleme');
  SELECT pg_temp.term('TECHNIQUE', 'tech-needle-lace-making', 'Needle lace making', parent => 'tech-needlework', local => 'Oya işleme');
  SELECT pg_temp.term('TECHNIQUE', 'tech-crochet', 'Crochet', parent => 'tech-needlework', local => 'Tığ işi');
  SELECT pg_temp.term('TECHNIQUE', 'tech-knitting', 'Knitting', parent => 'tech-needlework', local => 'Şiş örgüsü');
SELECT pg_temp.term('TECHNIQUE', 'tech-felting', 'Felting');
  SELECT pg_temp.term('TECHNIQUE', 'tech-wet-felting', 'Wet felting', parent => 'tech-felting', local => 'Islak keçe');
SELECT pg_temp.term('TECHNIQUE', 'tech-dyeing-and-printing', 'Dyeing and printing');
  SELECT pg_temp.term('TECHNIQUE', 'tech-natural-dyeing', 'Natural dyeing', parent => 'tech-dyeing-and-printing', local => 'Doğal boyama');
  SELECT pg_temp.term('TECHNIQUE', 'tech-mordanting', 'Mordanting', parent => 'tech-dyeing-and-printing', local => 'Mordanlama');
  SELECT pg_temp.term('TECHNIQUE', 'tech-resist-dyeing', 'Resist dyeing', parent => 'tech-dyeing-and-printing', local => 'Batik, bağlama boyama');
  SELECT pg_temp.term('TECHNIQUE', 'tech-block-printing', 'Block printing', parent => 'tech-dyeing-and-printing', local => 'Kalıpla baskı');
SELECT pg_temp.term('TECHNIQUE', 'tech-woodworking', 'Woodworking');
  SELECT pg_temp.term('TECHNIQUE', 'tech-carving', 'Carving', parent => 'tech-woodworking', local => 'Oyma');
  SELECT pg_temp.term('TECHNIQUE', 'tech-inlay-work', 'Inlay work', parent => 'tech-woodworking', local => 'Kakma');
  SELECT pg_temp.term('TECHNIQUE', 'tech-turning', 'Turning', parent => 'tech-woodworking', local => 'Tornalama');
  SELECT pg_temp.term('TECHNIQUE', 'tech-interlocking-joinery', 'Interlocking joinery', parent => 'tech-woodworking', local => 'Kündekari');
SELECT pg_temp.term('TECHNIQUE', 'tech-metalworking', 'Metalworking');
  SELECT pg_temp.term('TECHNIQUE', 'tech-forging', 'Forging', parent => 'tech-metalworking', local => 'Dövme');
  SELECT pg_temp.term('TECHNIQUE', 'tech-casting', 'Casting', parent => 'tech-metalworking', local => 'Döküm');
  SELECT pg_temp.term('TECHNIQUE', 'tech-hammering-and-raising', 'Hammering and raising', parent => 'tech-metalworking', local => 'Çekiçle şekillendirme');
  SELECT pg_temp.term('TECHNIQUE', 'tech-filigree', 'Filigree', parent => 'tech-metalworking', local => 'Telkari');
  SELECT pg_temp.term('TECHNIQUE', 'tech-engraving', 'Engraving', parent => 'tech-metalworking', local => 'Kalem işi, oyma');
  SELECT pg_temp.term('TECHNIQUE', 'tech-repousse', 'Repoussé', parent => 'tech-metalworking', local => 'Kabartma');
  SELECT pg_temp.term('TECHNIQUE', 'tech-tinning', 'Tinning', parent => 'tech-metalworking', local => 'Kalaylama');
SELECT pg_temp.term('TECHNIQUE', 'tech-basketry', 'Basketry');
  SELECT pg_temp.term('TECHNIQUE', 'tech-coiling', 'Coiling', parent => 'tech-basketry', local => 'Sarmal örme');
  SELECT pg_temp.term('TECHNIQUE', 'tech-plaiting', 'Plaiting', parent => 'tech-basketry', local => 'Hasır örme');
  SELECT pg_temp.term('TECHNIQUE', 'tech-twining', 'Twining', parent => 'tech-basketry', local => 'Bükerek örme');
SELECT pg_temp.term('TECHNIQUE', 'tech-paper-and-book-arts', 'Paper and book arts');
  SELECT pg_temp.term('TECHNIQUE', 'tech-marbling', 'Marbling', parent => 'tech-paper-and-book-arts', local => 'Ebru');
  SELECT pg_temp.term('TECHNIQUE', 'tech-gilding', 'Gilding', parent => 'tech-paper-and-book-arts', local => 'Yaldızlama');
  SELECT pg_temp.term('TECHNIQUE', 'tech-calligraphy-writing', 'Calligraphy writing', parent => 'tech-paper-and-book-arts', local => 'Hat yazımı');

-- ----------------------------------------------------------------------
-- Tools: with what is it made?
-- ----------------------------------------------------------------------
SELECT pg_temp.term('TOOL', 'tool-ceramics-tools', 'Ceramics tools');
  SELECT pg_temp.term('TOOL', 'tool-potter-s-wheel', 'Potter''s wheel', parent => 'tool-ceramics-tools', local => 'Çömlekçi çarkı');
  SELECT pg_temp.term('TOOL', 'tool-kiln', 'Kiln', parent => 'tool-ceramics-tools', local => 'Fırın');
  SELECT pg_temp.term('TOOL', 'tool-brush', 'Brush', parent => 'tool-ceramics-tools', local => 'Fırça');
SELECT pg_temp.term('TOOL', 'tool-textile-tools', 'Textile tools');
  SELECT pg_temp.term('TOOL', 'tool-loom', 'Loom', parent => 'tool-textile-tools', local => 'Dokuma tezgâhı');
    SELECT pg_temp.term('TOOL', 'tool-horizontal-loom', 'Horizontal loom', parent => 'tool-loom', local => 'Yer tezgâhı');
    SELECT pg_temp.term('TOOL', 'tool-vertical-loom', 'Vertical loom', parent => 'tool-loom', local => 'Dikey tezgâh');
  SELECT pg_temp.term('TOOL', 'tool-spindle', 'Spindle', parent => 'tool-textile-tools', local => 'İğ, kirman');
  SELECT pg_temp.term('TOOL', 'tool-carding-comb', 'Carding comb', parent => 'tool-textile-tools', local => 'Yün tarağı');
  SELECT pg_temp.term('TOOL', 'tool-embroidery-needle', 'Embroidery needle', parent => 'tool-textile-tools', local => 'Nakış iğnesi');
  SELECT pg_temp.term('TOOL', 'tool-crochet-hook', 'Crochet hook', parent => 'tool-textile-tools', local => 'Tığ');
SELECT pg_temp.term('TOOL', 'tool-woodwork-and-metalwork-tools', 'Woodwork and metalwork tools');
  SELECT pg_temp.term('TOOL', 'tool-chisel', 'Chisel', parent => 'tool-woodwork-and-metalwork-tools', local => 'Iskarpela, keski');
  SELECT pg_temp.term('TOOL', 'tool-lathe', 'Lathe', parent => 'tool-woodwork-and-metalwork-tools', local => 'Torna');
  SELECT pg_temp.term('TOOL', 'tool-hammer', 'Hammer', parent => 'tool-woodwork-and-metalwork-tools', local => 'Çekiç');
  SELECT pg_temp.term('TOOL', 'tool-anvil', 'Anvil', parent => 'tool-woodwork-and-metalwork-tools', local => 'Örs');
  SELECT pg_temp.term('TOOL', 'tool-burin', 'Burin', parent => 'tool-woodwork-and-metalwork-tools', local => 'Kalem', alt => 'engraving tool');
SELECT pg_temp.term('TOOL', 'tool-paper-and-book-arts-tools', 'Paper and book arts tools');
  SELECT pg_temp.term('TOOL', 'tool-marbling-trough', 'Marbling trough', parent => 'tool-paper-and-book-arts-tools', local => 'Ebru teknesi');
  SELECT pg_temp.term('TOOL', 'tool-horsehair-brush', 'Horsehair brush', parent => 'tool-paper-and-book-arts-tools', local => 'At kılı fırça');
  SELECT pg_temp.term('TOOL', 'tool-awl', 'Awl', parent => 'tool-paper-and-book-arts-tools', local => 'Biz');
  SELECT pg_temp.term('TOOL', 'tool-reed-pen', 'Reed pen', parent => 'tool-paper-and-book-arts-tools', local => 'Kamış kalem');

-- ----------------------------------------------------------------------
-- Colors: flat list with display color
-- ----------------------------------------------------------------------
SELECT pg_temp.term('COLOR', 'color-red', 'Red', local => 'Kırmızı', hex => '#C0392B');
SELECT pg_temp.term('COLOR', 'color-orange', 'Orange', local => 'Turuncu', hex => '#E67E22');
SELECT pg_temp.term('COLOR', 'color-yellow', 'Yellow', local => 'Sarı', hex => '#F1C40F');
SELECT pg_temp.term('COLOR', 'color-green', 'Green', local => 'Yeşil', hex => '#27AE60');
SELECT pg_temp.term('COLOR', 'color-blue', 'Blue', local => 'Mavi', hex => '#2E5FA8');
SELECT pg_temp.term('COLOR', 'color-turquoise', 'Turquoise', local => 'Turkuaz', hex => '#30B5B0');
SELECT pg_temp.term('COLOR', 'color-purple', 'Purple', local => 'Mor', hex => '#7D3C98');
SELECT pg_temp.term('COLOR', 'color-pink', 'Pink', local => 'Pembe', hex => '#E8A0BF');
SELECT pg_temp.term('COLOR', 'color-brown', 'Brown', local => 'Kahverengi', hex => '#8B5A2B');
SELECT pg_temp.term('COLOR', 'color-black', 'Black', local => 'Siyah', hex => '#1C1C1C');
SELECT pg_temp.term('COLOR', 'color-white', 'White', local => 'Beyaz', hex => '#FFFFFF');
SELECT pg_temp.term('COLOR', 'color-grey', 'Grey', local => 'Gri', hex => '#8C8C8C');
SELECT pg_temp.term('COLOR', 'color-gold', 'Gold', local => 'Altın rengi', hex => '#D4AF37');
SELECT pg_temp.term('COLOR', 'color-silver', 'Silver', local => 'Gümüş rengi', hex => '#C0C0C0');
SELECT pg_temp.term('COLOR', 'color-natural-undyed', 'Natural / undyed', local => 'Doğal, boyasız', hex => '#E8DCC4');

-- ----------------------------------------------------------------------
-- Regions: where does it come from?
-- ----------------------------------------------------------------------
SELECT pg_temp.term('REGION', 'region-asia', 'Asia', local => 'Asya');
  SELECT pg_temp.term('REGION', 'region-central-asia', 'Central Asia', parent => 'region-asia', local => 'Orta Asya');
    SELECT pg_temp.term('REGION', 'region-turkestan', 'Turkestan', parent => 'region-central-asia', local => 'Türkistan');
  SELECT pg_temp.term('REGION', 'region-east-asia', 'East Asia', parent => 'region-asia');
  SELECT pg_temp.term('REGION', 'region-south-asia', 'South Asia', parent => 'region-asia');
  SELECT pg_temp.term('REGION', 'region-southeast-asia', 'Southeast Asia', parent => 'region-asia');
  SELECT pg_temp.term('REGION', 'region-western-asia', 'Western Asia', parent => 'region-asia', local => 'Batı Asya');
    SELECT pg_temp.term('REGION', 'region-anatolia', 'Anatolia', parent => 'region-western-asia', local => 'Anadolu');
      SELECT pg_temp.term('REGION', 'region-marmara', 'Marmara', parent => 'region-anatolia', local => 'Marmara Bölgesi');
        SELECT pg_temp.term('REGION', 'region-iznik', 'İznik', parent => 'region-marmara', local => 'İznik');
      SELECT pg_temp.term('REGION', 'region-aegean', 'Aegean', parent => 'region-anatolia', local => 'Ege Bölgesi');
        SELECT pg_temp.term('REGION', 'region-kutahya', 'Kütahya', parent => 'region-aegean', local => 'Kütahya');
      SELECT pg_temp.term('REGION', 'region-mediterranean', 'Mediterranean', parent => 'region-anatolia', local => 'Akdeniz Bölgesi');
      SELECT pg_temp.term('REGION', 'region-central-anatolia', 'Central Anatolia', parent => 'region-anatolia', local => 'İç Anadolu Bölgesi');
      SELECT pg_temp.term('REGION', 'region-black-sea', 'Black Sea', parent => 'region-anatolia', local => 'Karadeniz Bölgesi');
      SELECT pg_temp.term('REGION', 'region-eastern-anatolia', 'Eastern Anatolia', parent => 'region-anatolia', local => 'Doğu Anadolu Bölgesi');
      SELECT pg_temp.term('REGION', 'region-southeastern-anatolia', 'Southeastern Anatolia', parent => 'region-anatolia', local => 'Güneydoğu Anadolu Bölgesi');
    SELECT pg_temp.term('REGION', 'region-iran', 'Iran', parent => 'region-western-asia', local => 'İran');
    SELECT pg_temp.term('REGION', 'region-caucasus', 'Caucasus', parent => 'region-western-asia', local => 'Kafkasya');
    SELECT pg_temp.term('REGION', 'region-levant', 'Levant', parent => 'region-western-asia');
    SELECT pg_temp.term('REGION', 'region-arabian-peninsula', 'Arabian Peninsula', parent => 'region-western-asia', local => 'Arap Yarımadası');
SELECT pg_temp.term('REGION', 'region-europe', 'Europe', local => 'Avrupa');
  SELECT pg_temp.term('REGION', 'region-balkans', 'Balkans', parent => 'region-europe', local => 'Balkanlar');
    SELECT pg_temp.term('REGION', 'region-thrace', 'Thrace', parent => 'region-balkans', local => 'Trakya');
  SELECT pg_temp.term('REGION', 'region-eastern-europe', 'Eastern Europe', parent => 'region-europe');
  SELECT pg_temp.term('REGION', 'region-western-europe', 'Western Europe', parent => 'region-europe');
  SELECT pg_temp.term('REGION', 'region-northern-europe', 'Northern Europe', parent => 'region-europe');
  SELECT pg_temp.term('REGION', 'region-southern-europe', 'Southern Europe', parent => 'region-europe');
SELECT pg_temp.term('REGION', 'region-africa', 'Africa', local => 'Afrika');
  SELECT pg_temp.term('REGION', 'region-north-africa', 'North Africa', parent => 'region-africa');
  SELECT pg_temp.term('REGION', 'region-west-africa', 'West Africa', parent => 'region-africa');
  SELECT pg_temp.term('REGION', 'region-east-africa', 'East Africa', parent => 'region-africa');
  SELECT pg_temp.term('REGION', 'region-central-africa', 'Central Africa', parent => 'region-africa');
  SELECT pg_temp.term('REGION', 'region-southern-africa', 'Southern Africa', parent => 'region-africa');
SELECT pg_temp.term('REGION', 'region-americas', 'Americas', local => 'Amerika');
  SELECT pg_temp.term('REGION', 'region-north-america', 'North America', parent => 'region-americas');
  SELECT pg_temp.term('REGION', 'region-central-america', 'Central America', parent => 'region-americas');
  SELECT pg_temp.term('REGION', 'region-caribbean', 'Caribbean', parent => 'region-americas');
  SELECT pg_temp.term('REGION', 'region-south-america', 'South America', parent => 'region-americas');
    SELECT pg_temp.term('REGION', 'region-andes', 'Andes', parent => 'region-south-america');
SELECT pg_temp.term('REGION', 'region-oceania', 'Oceania', local => 'Okyanusya');
  SELECT pg_temp.term('REGION', 'region-australia', 'Australia', parent => 'region-oceania');
  SELECT pg_temp.term('REGION', 'region-melanesia', 'Melanesia', parent => 'region-oceania');
  SELECT pg_temp.term('REGION', 'region-micronesia', 'Micronesia', parent => 'region-oceania');
  SELECT pg_temp.term('REGION', 'region-polynesia', 'Polynesia', parent => 'region-oceania');

-- ----------------------------------------------------------------------
-- Periods: when did it emerge? (years: negative = BCE)
-- ----------------------------------------------------------------------
SELECT pg_temp.term('PERIOD', 'period-prehistoric', 'Prehistoric', local => 'Tarih öncesi', y_to => -3000);
  SELECT pg_temp.term('PERIOD', 'period-neolithic-anatolia', 'Neolithic Anatolia', parent => 'period-prehistoric', local => 'Anadolu Neolitiği', y_from => -10000, y_to => -5500);
SELECT pg_temp.term('PERIOD', 'period-ancient', 'Ancient', local => 'Antik Çağ', y_from => -3000, y_to => 500);
  SELECT pg_temp.term('PERIOD', 'period-hittite', 'Hittite', parent => 'period-ancient', local => 'Hitit', y_from => -1650, y_to => -1180);
  SELECT pg_temp.term('PERIOD', 'period-phrygian', 'Phrygian', parent => 'period-ancient', local => 'Frig', y_from => -1200, y_to => -675);
  SELECT pg_temp.term('PERIOD', 'period-roman', 'Roman', parent => 'period-ancient', local => 'Roma', y_from => -27, y_to => 476);
SELECT pg_temp.term('PERIOD', 'period-medieval', 'Medieval', local => 'Orta Çağ', y_from => 500, y_to => 1500);
  SELECT pg_temp.term('PERIOD', 'period-byzantine', 'Byzantine', parent => 'period-medieval', local => 'Bizans', y_from => 330, y_to => 1453);
  SELECT pg_temp.term('PERIOD', 'period-seljuk', 'Seljuk', parent => 'period-medieval', local => 'Selçuklu', y_from => 1037, y_to => 1308);
  SELECT pg_temp.term('PERIOD', 'period-timurid', 'Timurid', parent => 'period-medieval', local => 'Timurlu', y_from => 1370, y_to => 1507);
SELECT pg_temp.term('PERIOD', 'period-early-modern', 'Early modern', local => 'Erken Modern Çağ', y_from => 1500, y_to => 1800);
  SELECT pg_temp.term('PERIOD', 'period-ottoman-classical-period', 'Ottoman classical period', parent => 'period-early-modern', local => 'Klasik Osmanlı dönemi', y_from => 1453, y_to => 1600);
  SELECT pg_temp.term('PERIOD', 'period-safavid', 'Safavid', parent => 'period-early-modern', local => 'Safevi', y_from => 1501, y_to => 1736);
SELECT pg_temp.term('PERIOD', 'period-modern', 'Modern', local => 'Modern Çağ', y_from => 1800, y_to => 1950);
  SELECT pg_temp.term('PERIOD', 'period-late-ottoman', 'Late Ottoman', parent => 'period-modern', local => 'Geç Osmanlı', y_from => 1800, y_to => 1922);
  SELECT pg_temp.term('PERIOD', 'period-early-republic', 'Early Republic', parent => 'period-modern', local => 'Erken Cumhuriyet', y_from => 1923, y_to => 1950);
SELECT pg_temp.term('PERIOD', 'period-contemporary', 'Contemporary', local => 'Günümüz', y_from => 1950);

DROP FUNCTION pg_temp.term(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, INT, INT);
