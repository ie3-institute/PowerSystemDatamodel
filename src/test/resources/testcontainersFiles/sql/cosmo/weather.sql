CREATE TABLE public.weather
(
    time timestamp with time zone,
    coordinate_id integer,
    diffuse_irradiance double precision,
    direct_irradiance double precision,
    wind_direction double precision,
    wind_velocity double precision,
    temperature double precision,
    tid serial,
    CONSTRAINT weather_pkey PRIMARY KEY (tid),
    CONSTRAINT "weather_datum_coordinate_id_unique" UNIQUE (time, coordinate_id)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

CREATE INDEX weather_coordinate_id_idx
    ON public.weather USING btree
        (coordinate_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX weather_coordinate_id_time_idx
    ON public.weather USING btree
        (coordinate_id ASC NULLS LAST, time ASC NULLS LAST)
    TABLESPACE pg_default;

INSERT INTO
    public.weather (time, coordinate_id, diffuse_irradiance, direct_irradiance, wind_direction, wind_velocity, temperature)
VALUES
('2020-04-28 15:00:00+0', 193186, 286.872985839844, 282.671997070312, 0, 1.66103506088257, 278.019012451172),
('2020-04-28 15:00:00+0', 193187, 287.872985839844, 283.671997070312, 0, 1.76103506088257, 279.019012451172),
('2020-04-28 15:00:00+0', 193188, 288.872985839844, 284.671997070312, 0, 1.86103506088257, 280.019012451172),
('2020-04-28 16:00:00+0', 193186, 286.872, 282.672, 0, 1.662, 278.012),
('2020-04-28 16:00:00+0', 193187, 287.872, 283.672, 0, 1.762, 279.012),
('2020-04-28 17:00:00+0', 193186, 286.873, 282.673, 0, 1.663, 278.013);
