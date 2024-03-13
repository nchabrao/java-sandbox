CREATE TABLE openings (
id SERIAL PRIMARY KEY,
doctor_name text,
starts_at timestamp,
ends_at timestamp,
slot_size_min numeric(2,0) 
);