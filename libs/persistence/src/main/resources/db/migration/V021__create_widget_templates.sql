CREATE TABLE widget_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) DEFAULT 'general',
    schema_json JSONB NOT NULL DEFAULT '{}',
    html_template TEXT NOT NULL,
    css_code TEXT,
    js_code TEXT,
    default_data_json JSONB DEFAULT '{}',
    preview_html TEXT,
    is_active BOOLEAN DEFAULT true,
    version INT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_widget_templates_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_widget_templates_tenant ON widget_templates(tenant_id);
CREATE INDEX idx_widget_templates_category ON widget_templates(category);

-- Seed 15 global templates (tenant_id = NULL)

-- 1. TICKER
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'ticker',
    'Ticker',
    'Scrolling text ticker at bottom of screen',
    'text',
    '{"type":"object","properties":{"text":{"type":"string","title":"Ticker Text","format":"textarea"},"label":{"type":"string","title":"Label"},"scrollSpeed":{"type":"integer","title":"Scroll Speed","default":60,"minimum":10,"maximum":200},"position":{"type":"string","title":"Position","enum":["bottom","top"],"default":"bottom"}}}',
    '<div class="overlay-ticker"><div class="ticker-label">{{label}}</div><div class="ticker-track"><span class="ticker-text" style="animation-duration:{{duration}}s">{{text}}</span></div></div>',
    '.overlay-ticker { position: absolute; bottom: 0; left: 0; width: 100%; height: 48px; background: rgba(0,0,0,0.85); color: #fff; display: flex; align-items: center; overflow: hidden; pointer-events: auto; } .overlay-ticker .ticker-label { background: #e50914; color: #fff; font-weight: 700; font-size: 14px; padding: 0 16px; height: 100%; display: flex; align-items: center; white-space: nowrap; flex-shrink: 0; text-transform: uppercase; } .overlay-ticker .ticker-track { flex: 1; overflow: hidden; position: relative; height: 100%; display: flex; align-items: center; } .overlay-ticker .ticker-text { white-space: nowrap; font-size: 16px; font-weight: 500; animation: ticker-scroll linear infinite; padding-left: 100%; } @keyframes ticker-scroll { 0% { transform: translateX(0); } 100% { transform: translateX(-100%); } }',
    NULL,
    '{"text":"Overlay active — edit data in the binding settings","label":"BREAKING","scrollSpeed":60,"position":"bottom"}',
    '<div class="tv-ticker-label">BREAKING</div><div class="tv-ticker-bar"><div class="tv-ticker-text">Breaking news: live update scrolling across the screen...</div></div>'
);

-- 2. KPI_TILES
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'kpi-tiles',
    'KPI Tiles',
    'Grid of key performance indicators',
    'data',
    '{"type":"object","properties":{"kpis":{"type":"array","title":"KPI Tiles","items":{"type":"object","properties":{"label":{"type":"string","title":"Label"},"value":{"type":"string","title":"Value"},"color":{"type":"string","title":"Color","format":"color","default":"#3b82f6"}}},"default":[{"label":"Sample","value":"0","color":"#3b82f6"}]},"columns":{"type":"integer","title":"Grid Columns","default":2,"minimum":1,"maximum":6},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right","center"],"default":"top-right"}}}',
    '<div class="overlay-kpi-grid {{posClass}}" style="grid-template-columns:repeat({{columns}},140px)">{{#kpis}}<div class="overlay-kpi-tile"><div class="kpi-value" style="color:{{color}}">{{value}}</div><div class="kpi-label">{{label}}</div></div>{{/kpis}}</div>',
    '.overlay-kpi-grid { position: absolute; display: grid; gap: 8px; padding: 12px; } .overlay-kpi-tile { background: rgba(0,0,0,0.8); border-radius: 8px; padding: 12px 16px; color: #fff; text-align: center; backdrop-filter: blur(8px); border: 1px solid rgba(255,255,255,0.1); } .overlay-kpi-tile .kpi-value { font-size: 28px; font-weight: 700; line-height: 1.2; } .overlay-kpi-tile .kpi-label { font-size: 11px; text-transform: uppercase; letter-spacing: 0.5px; opacity: 0.7; margin-top: 4px; }',
    NULL,
    '{"kpis":[{"label":"Sample","value":"0","color":"#3b82f6"}],"columns":2,"position":"top-right"}',
    '<div class="tv-kpi-grid"><div class="tv-kpi-tile"><div class="tv-kpi-value">1,247</div><div class="tv-kpi-label">Viewers</div></div><div class="tv-kpi-tile"><div class="tv-kpi-value">89%</div><div class="tv-kpi-label">Uptime</div></div><div class="tv-kpi-tile"><div class="tv-kpi-value">42</div><div class="tv-kpi-label">Active</div></div><div class="tv-kpi-tile"><div class="tv-kpi-value">3.2k</div><div class="tv-kpi-label">Total</div></div></div>'
);

-- 3. QUEUE_TABLE
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'queue-table',
    'Queue Table',
    'Dynamic table showing queue data',
    'data',
    '{"type":"object","properties":{"columns":{"type":"array","title":"Column Headers","items":{"type":"string"},"default":["Name","Status","Wait"]},"rows":{"type":"array","title":"Table Rows","items":{"type":"array","items":{"type":"string"}},"default":[["Sample","OK","0m"]]},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right","center"],"default":"center"}}}',
    '<div class="overlay-table {{posClass}}"><table>{{#hasColumns}}<thead><tr>{{#columns}}<th>{{.}}</th>{{/columns}}</tr></thead>{{/hasColumns}}<tbody>{{#rows}}<tr>{{#.}}<td>{{.}}</td>{{/.}}</tr>{{/rows}}</tbody></table></div>',
    '.overlay-table { position: absolute; background: rgba(0,0,0,0.85); color: #fff; border-radius: 8px; padding: 12px; backdrop-filter: blur(8px); max-width: 500px; } .overlay-table table { width: 100%; border-collapse: collapse; font-size: 13px; } .overlay-table th { text-align: left; padding: 6px 8px; border-bottom: 1px solid rgba(255,255,255,0.2); font-weight: 600; text-transform: uppercase; font-size: 11px; opacity: 0.7; } .overlay-table td { padding: 6px 8px; border-bottom: 1px solid rgba(255,255,255,0.08); }',
    NULL,
    '{"columns":["Name","Status","Wait"],"rows":[["Sample","OK","0m"]],"position":"center"}',
    '<div class="tv-table"><div class="tv-table-header"><span>Name</span><span>Status</span><span>Wait</span></div><div class="tv-table-row"><span>Alice K.</span><span class="tv-dot tv-dot-green"></span><span>2m</span></div><div class="tv-table-row"><span>Bob M.</span><span class="tv-dot tv-dot-yellow"></span><span>5m</span></div><div class="tv-table-row"><span>Carol D.</span><span class="tv-dot tv-dot-red"></span><span>12m</span></div></div>'
);

-- 4. QR_CARD
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'qr-card',
    'QR Card',
    'Card with QR code and call to action',
    'media',
    '{"type":"object","properties":{"url":{"type":"string","title":"QR Code URL","format":"url"},"text":{"type":"string","title":"Call to Action Text"},"size":{"type":"integer","title":"QR Size (px)","default":120,"minimum":80,"maximum":300},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right","center"],"default":"bottom-right"}}}',
    '<div class="overlay-qr-card {{posClass}}"><img src="https://api.qrserver.com/v1/create-qr-code/?size={{size}}x{{size}}&data={{urlEncoded}}" width="{{size}}" height="{{size}}">{{#text}}<div class="qr-text">{{text}}</div>{{/text}}</div>',
    '.overlay-qr-card { position: absolute; background: #fff; border-radius: 12px; padding: 16px; text-align: center; box-shadow: 0 4px 20px rgba(0,0,0,0.3); } .overlay-qr-card canvas { display: block; margin: 0 auto; } .overlay-qr-card .qr-text { margin-top: 8px; font-size: 12px; color: #333; font-weight: 600; }',
    NULL,
    '{"url":"https://example.com","text":"Scan to join","size":120,"position":"bottom-right"}',
    '<div class="tv-qr-card"><div class="tv-qr-box"><div class="tv-qr-cell"></div><div class="tv-qr-cell"></div><div class="tv-qr-cell"></div><div class="tv-qr-cell"></div><div class="tv-qr-cell"></div><div class="tv-qr-cell"></div><div class="tv-qr-cell"></div><div class="tv-qr-cell"></div><div class="tv-qr-cell"></div></div><div class="tv-qr-text"><div class="tv-qr-cta">Scan to join</div><div class="tv-qr-url">example.com</div></div></div>'
);

-- 5. POLL
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'poll',
    'Poll',
    'Interactive poll with voting',
    'data',
    '{"type":"object","properties":{"question":{"type":"string","title":"Poll Question"},"options":{"type":"array","title":"Poll Options","items":{"type":"string"},"default":["Option A","Option B"]},"votes":{"type":"array","title":"Vote Counts","items":{"type":"integer"},"default":[0,0]},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right","center"],"default":"center"}}}',
    '<div class="overlay-poll {{posClass}}"><div class="poll-question">{{question}}</div>{{#options}}<div class="poll-option"><div class="poll-bar-bg"><div class="poll-bar-fill" style="width:{{percent}}%"></div><div class="poll-bar-label"><span>{{text}}</span><span>{{percent}}%</span></div></div></div>{{/options}}</div>',
    '.overlay-poll { position: absolute; background: rgba(0,0,0,0.85); color: #fff; border-radius: 12px; padding: 16px; backdrop-filter: blur(8px); min-width: 280px; } .overlay-poll .poll-question { font-size: 16px; font-weight: 700; margin-bottom: 12px; } .overlay-poll .poll-option { margin-bottom: 8px; } .overlay-poll .poll-bar-bg { background: rgba(255,255,255,0.15); border-radius: 4px; height: 28px; position: relative; overflow: hidden; } .overlay-poll .poll-bar-fill { height: 100%; border-radius: 4px; transition: width 0.5s ease; background: linear-gradient(90deg, #667eea, #764ba2); } .overlay-poll .poll-bar-label { position: absolute; top: 0; left: 8px; right: 8px; height: 28px; display: flex; align-items: center; justify-content: space-between; font-size: 13px; font-weight: 500; }',
    NULL,
    '{"question":"What do you prefer?","options":["Option A","Option B"],"votes":[0,0],"position":"center"}',
    '<div class="tv-poll"><div class="tv-poll-question">What do you prefer?</div><div class="tv-poll-option"><div class="tv-poll-bar" style="width:65%">Option A — 65%</div></div><div class="tv-poll-option"><div class="tv-poll-bar tv-poll-bar-alt" style="width:35%">Option B — 35%</div></div></div>'
);

-- 6. REACTIONS
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'reactions',
    'Reactions',
    'Emoji reaction overlay',
    'interactive',
    '{"type":"object","properties":{"emojis":{"type":"array","title":"Available Emojis","items":{"type":"string"},"default":["👍","❤️","😂","🎉","👏"]},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right","center"],"default":"bottom-right"}}}',
    '<div class="overlay-reactions {{posClass}}" style="width:200px;height:150px">{{#recent}}<span class="overlay-reaction-emoji" style="left:{{left}}px;bottom:0;animation-delay:{{delay}}s">{{emoji}}</span>{{/recent}}</div>',
    '.overlay-reactions { position: absolute; pointer-events: auto; } .overlay-reaction-emoji { position: absolute; font-size: 32px; animation: reaction-float 3s ease-out forwards; opacity: 0; } @keyframes reaction-float { 0% { opacity: 1; transform: translateY(0) scale(1); } 100% { opacity: 0; transform: translateY(-120px) scale(0.5); } }',
    NULL,
    '{"emojis":["👍","❤️","😂","🎉","👏"],"position":"bottom-right"}',
    '<div class="tv-reactions"><span class="tv-reaction-bubble">👍</span><span class="tv-reaction-bubble">❤️</span><span class="tv-reaction-bubble">😂</span><span class="tv-reaction-bubble">🎉</span><span class="tv-reaction-bubble">👏</span></div>'
);

-- 7. WEATHER
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'weather',
    'Weather',
    'Live weather widget',
    'info',
    '{"type":"object","properties":{"temp":{"type":"string","title":"Temperature"},"description":{"type":"string","title":"Description"},"icon":{"type":"string","title":"Weather Icon (emoji)","default":"🌤"},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right","center"],"default":"top-right"}}}',
    '<div class="overlay-weather {{posClass}}"><span class="weather-icon">{{icon}}</span><div><div class="weather-temp">{{temp}}°</div><div class="weather-desc">{{description}}</div></div></div>',
    '.overlay-weather { position: absolute; background: rgba(0,0,0,0.75); color: #fff; border-radius: 8px; padding: 12px 16px; backdrop-filter: blur(8px); display: flex; align-items: center; gap: 12px; } .overlay-weather .weather-icon { font-size: 32px; } .overlay-weather .weather-temp { font-size: 24px; font-weight: 700; } .overlay-weather .weather-desc { font-size: 12px; opacity: 0.7; }',
    NULL,
    '{"temp":"24","description":"Sunny","icon":"☀️","position":"top-right"}',
    '<div class="tv-weather"><div class="tv-weather-icon">☀️</div><div class="tv-weather-info"><div class="tv-weather-temp">+24°C</div><div class="tv-weather-city">Almaty</div></div></div>'
);

-- 8. CLOCK
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'clock',
    'Clock',
    'Current time display',
    'time',
    '{"type":"object","properties":{"timezone":{"type":"string","title":"Timezone","default":"Asia/Almaty"},"format":{"type":"string","title":"Time Format","enum":["24h","12h"],"default":"24h"},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right","center"],"default":"top-right"}}}',
    '<div class="overlay-clock {{posClass}}" id="clock-{{id}}"></div>',
    '.overlay-clock { position: absolute; background: rgba(0,0,0,0.75); color: #fff; border-radius: 8px; padding: 8px 16px; backdrop-filter: blur(8px); font-size: 24px; font-weight: 600; font-variant-numeric: tabular-nums; }',
    'function initClock(el, tz, fmt) { function tick() { try { var opts = { timeZone: tz, hour: "2-digit", minute: "2-digit", second: "2-digit" }; if (fmt === "12h") opts.hour12 = true; else opts.hour12 = false; el.textContent = new Date().toLocaleTimeString("en-GB", opts); } catch(e) { el.textContent = new Date().toLocaleTimeString(); } } tick(); setInterval(tick, 1000); }',
    '{"timezone":"Asia/Almaty","format":"24h","position":"top-right"}',
    '<div class="tv-clock">14:30:05</div>'
);

-- 9. NEWS_TICKER
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'news-ticker',
    'News Ticker',
    'Scrolling news feed from RSS/URL',
    'text',
    '{"type":"object","properties":{"items":{"type":"array","title":"News Items","items":{"type":"string"},"default":["First news item","Second news item","Third news item"]},"scrollSpeed":{"type":"integer","title":"Scroll Speed","default":60,"minimum":10,"maximum":200},"separator":{"type":"string","title":"Separator","default":"●"},"position":{"type":"string","title":"Position","enum":["bottom","top"],"default":"bottom"}}}',
    '<div class="overlay-ticker"><div class="ticker-label">NEWS</div><div class="ticker-track"><span class="ticker-text" style="animation-duration:{{duration}}s">{{itemsText}}</span></div></div>',
    '.overlay-ticker { position: absolute; bottom: 0; left: 0; width: 100%; height: 48px; background: rgba(0,0,0,0.85); color: #fff; display: flex; align-items: center; overflow: hidden; pointer-events: auto; } .overlay-ticker .ticker-label { background: #e50914; color: #fff; font-weight: 700; font-size: 14px; padding: 0 16px; height: 100%; display: flex; align-items: center; white-space: nowrap; flex-shrink: 0; text-transform: uppercase; } .overlay-ticker .ticker-track { flex: 1; overflow: hidden; position: relative; height: 100%; display: flex; align-items: center; } .overlay-ticker .ticker-text { white-space: nowrap; font-size: 16px; font-weight: 500; animation: ticker-scroll linear infinite; padding-left: 100%; } @keyframes ticker-scroll { 0% { transform: translateX(0); } 100% { transform: translateX(-100%); } }',
    NULL,
    '{"items":["First news item","Second news item","Third news item"],"scrollSpeed":60,"separator":"●","position":"bottom"}',
    '<div class="tv-news-rss">RSS</div><div class="tv-ticker-bar"><div class="tv-ticker-text">Latest headlines: markets up 2.3% today, weather forecast clear...</div></div>'
);

-- 10. LOGO
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'logo',
    'Logo',
    'Channel logo watermark (bug)',
    'media',
    '{"type":"object","properties":{"imageUrl":{"type":"string","title":"Logo Image URL","format":"url"},"width":{"type":"integer","title":"Logo Width (px)","default":80,"minimum":40,"maximum":300},"opacity":{"type":"number","title":"Opacity","default":0.7,"minimum":0,"maximum":1,"multipleOf":0.1},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right"],"default":"top-right"}}}',
    '<div class="overlay-logo {{posClass}}" style="--logo-width:{{width}}px;opacity:{{opacity}}"><img src="{{imageUrl}}" alt="Logo"></div>',
    '.overlay-logo { position: absolute; pointer-events: none; } .overlay-logo img { display: block; width: var(--logo-width, 80px); height: auto; }',
    NULL,
    '{"imageUrl":"","width":80,"opacity":0.7,"position":"top-right"}',
    '<div class="tv-logo"><div class="tv-logo-placeholder">LOGO</div></div>'
);

-- 11. ANIMATED_LOGO
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'animated-logo',
    'Animated Logo',
    'Logo with CSS animation (pulse, bounce, spin, fade)',
    'media',
    '{"type":"object","properties":{"imageUrl":{"type":"string","title":"Logo Image URL","format":"url"},"width":{"type":"integer","title":"Logo Width (px)","default":80,"minimum":40,"maximum":300},"animation":{"type":"string","title":"Animation Style","enum":["pulse","bounce","spin","fade"],"default":"pulse"},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right"],"default":"top-right"}}}',
    '<div class="overlay-animated-logo anim-{{animation}} {{posClass}}" style="--logo-width:{{width}}px"><img src="{{imageUrl}}" alt="Logo"></div>',
    '.overlay-animated-logo { position: absolute; pointer-events: none; } .overlay-animated-logo img { display: block; width: var(--logo-width, 80px); height: auto; } @keyframes logo-pulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.08); } } @keyframes logo-bounce { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-8px); } } @keyframes logo-spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } } @keyframes logo-fade { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } } .anim-pulse img { animation: logo-pulse 2s ease-in-out infinite; } .anim-bounce img { animation: logo-bounce 1.5s ease-in-out infinite; } .anim-spin img { animation: logo-spin 3s linear infinite; } .anim-fade img { animation: logo-fade 2.5s ease-in-out infinite; }',
    NULL,
    '{"imageUrl":"","width":80,"animation":"pulse","position":"top-right"}',
    '<div class="tv-logo tv-logo-animated"><div class="tv-logo-placeholder tv-logo-pulse">LOGO</div></div>'
);

-- 12. LOWER_THIRD
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'lower-third',
    'Lower Third',
    'Name + title bar with slide-in animation',
    'info',
    '{"type":"object","properties":{"name":{"type":"string","title":"Name"},"title":{"type":"string","title":"Title/Role"},"accentColor":{"type":"string","title":"Accent Color","format":"color","default":"#e50914"}}}',
    '<div class="overlay-lower-third"><div class="lt-accent" style="background:{{accentColor}}"></div><div class="lt-content"><div class="lt-name">{{name}}</div>{{#title}}<div class="lt-title">{{title}}</div>{{/title}}</div></div>',
    '.overlay-lower-third { position: absolute; bottom: 60px; left: 0; display: flex; align-items: stretch; animation: lt-slide-in 0.6s ease-out forwards; pointer-events: none; } @keyframes lt-slide-in { 0% { transform: translateX(-100%); opacity: 0; } 100% { transform: translateX(0); opacity: 1; } } .overlay-lower-third .lt-accent { width: 6px; flex-shrink: 0; } .overlay-lower-third .lt-content { background: rgba(0,0,0,0.85); color: #fff; padding: 10px 24px; backdrop-filter: blur(8px); } .overlay-lower-third .lt-name { font-size: 20px; font-weight: 700; line-height: 1.3; } .overlay-lower-third .lt-title { font-size: 13px; opacity: 0.7; text-transform: uppercase; letter-spacing: 0.5px; }',
    NULL,
    '{"name":"John Doe","title":"Correspondent","accentColor":"#e50914"}',
    '<div class="tv-lower-third"><div class="tv-lt-accent"></div><div class="tv-lt-content"><div class="tv-lt-name">John Doe</div><div class="tv-lt-title">Correspondent</div></div></div>'
);

-- 13. SCORE
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'score',
    'Score',
    'Sports scoreboard with teams, score and period',
    'sports',
    '{"type":"object","properties":{"team1":{"type":"string","title":"Team 1 Name","default":"Home"},"team2":{"type":"string","title":"Team 2 Name","default":"Away"},"score1":{"type":"integer","title":"Team 1 Score","default":0},"score2":{"type":"integer","title":"Team 2 Score","default":0},"period":{"type":"string","title":"Period/Quarter"},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right","center"],"default":"top-center"}}}',
    '<div class="overlay-score {{posClass}}"><div class="score-team">{{team1}}</div><div class="score-num">{{score1}}</div><div class="score-divider">:</div><div class="score-num">{{score2}}</div><div class="score-team">{{team2}}</div>{{#period}}<div class="score-period">{{period}}</div>{{/period}}</div>',
    '.overlay-score { position: absolute; background: rgba(0,0,0,0.9); color: #fff; border-radius: 8px; display: flex; align-items: center; padding: 0; overflow: hidden; backdrop-filter: blur(8px); font-family: "Segoe UI", Arial, sans-serif; } .overlay-score .score-team { padding: 8px 16px; font-size: 14px; font-weight: 600; text-transform: uppercase; min-width: 90px; text-align: center; } .overlay-score .score-num { background: rgba(255,255,255,0.15); padding: 8px 14px; font-size: 24px; font-weight: 800; font-variant-numeric: tabular-nums; min-width: 40px; text-align: center; } .overlay-score .score-divider { padding: 8px 4px; font-size: 18px; font-weight: 700; opacity: 0.5; } .overlay-score .score-period { padding: 8px 14px; font-size: 11px; text-transform: uppercase; opacity: 0.6; border-left: 1px solid rgba(255,255,255,0.15); }',
    NULL,
    '{"team1":"Home","team2":"Away","score1":0,"score2":0,"period":"","position":"top-center"}',
    '<div class="tv-score"><span class="tv-score-team">HOME</span><span class="tv-score-num">2</span><span class="tv-score-div">:</span><span class="tv-score-num">1</span><span class="tv-score-team">AWAY</span></div>'
);

-- 14. BREAKING_NEWS
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'breaking-news',
    'Breaking News',
    'Full-width breaking news alert bar',
    'text',
    '{"type":"object","properties":{"headline":{"type":"string","title":"Headline Text"},"label":{"type":"string","title":"Label","default":"BREAKING"},"severity":{"type":"string","title":"Severity","enum":["red","yellow","blue"],"default":"red"}}}',
    '<div class="overlay-breaking breaking-severity-{{severity}}"><div class="breaking-label">{{label}}</div><div class="breaking-text">{{headline}}</div></div>',
    '.overlay-breaking { position: absolute; bottom: 0; left: 0; width: 100%; display: flex; align-items: center; height: 52px; pointer-events: auto; } .overlay-breaking .breaking-label { height: 100%; display: flex; align-items: center; padding: 0 18px; font-weight: 800; font-size: 15px; color: #fff; text-transform: uppercase; letter-spacing: 1px; flex-shrink: 0; animation: breaking-flash 1s ease-in-out infinite; } @keyframes breaking-flash { 0%,100% { opacity: 1; } 50% { opacity: 0.6; } } .breaking-severity-red .breaking-label { background: #dc2626; } .breaking-severity-yellow .breaking-label { background: #d97706; } .breaking-severity-blue .breaking-label { background: #2563eb; } .overlay-breaking .breaking-text { flex: 1; background: rgba(0,0,0,0.9); color: #fff; height: 100%; display: flex; align-items: center; padding: 0 20px; font-size: 16px; font-weight: 500; }',
    NULL,
    '{"headline":"Major event unfolding live...","label":"BREAKING","severity":"red"}',
    '<div class="tv-breaking"><span class="tv-breaking-label">BREAKING</span><span class="tv-breaking-text">Major event unfolding live...</span></div>'
);

-- 15. COUNTDOWN
INSERT INTO widget_templates (tenant_id, code, name, description, category, schema_json, html_template, css_code, js_code, default_data_json, preview_html) VALUES (
    NULL,
    'countdown',
    'Countdown',
    'Countdown timer to a target time',
    'time',
    '{"type":"object","properties":{"targetTime":{"type":"string","title":"Target Date/Time","format":"datetime-local"},"label":{"type":"string","title":"Label Text"},"position":{"type":"string","title":"Position","enum":["top-left","top-right","top-center","bottom-left","bottom-right","center"],"default":"center"}}}',
    '<div class="overlay-countdown {{posClass}}" id="countdown-{{id}}">{{#label}}<div class="cd-label">{{label}}</div>{{/label}}<div class="cd-time">--:--:--</div></div>',
    '.overlay-countdown { position: absolute; background: rgba(0,0,0,0.8); color: #fff; border-radius: 12px; padding: 16px 24px; text-align: center; backdrop-filter: blur(8px); } .overlay-countdown .cd-label { font-size: 12px; text-transform: uppercase; letter-spacing: 1px; opacity: 0.7; margin-bottom: 8px; } .overlay-countdown .cd-time { font-size: 36px; font-weight: 800; font-variant-numeric: tabular-nums; letter-spacing: 2px; }',
    'function initCountdown(el, target) { if (!target) return; var targetDate = new Date(target); var timeDiv = el.querySelector(".cd-time"); function tick() { var now = new Date(); var diff = Math.max(0, Math.floor((targetDate - now) / 1000)); var h = Math.floor(diff / 3600); var m = Math.floor((diff % 3600) / 60); var s = diff % 60; if (diff <= 0) { timeDiv.textContent = "00:00:00"; } else { timeDiv.textContent = String(h).padStart(2,"0") + ":" + String(m).padStart(2,"0") + ":" + String(s).padStart(2,"0"); } } tick(); setInterval(tick, 1000); }',
    '{"targetTime":"","label":"","position":"center"}',
    '<div class="tv-countdown"><div class="tv-cd-label">STARTING IN</div><div class="tv-cd-time">01:23:45</div></div>'
);
