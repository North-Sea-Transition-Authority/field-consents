const sass = require("gulp-sass")(require("sass"));
const postcss = require("gulp-postcss");
const gulp = require("gulp");
const sourcemaps = require("gulp-sourcemaps");
const autoprefixer = require("autoprefixer");
const rename = require("gulp-rename");

const rollup = require("rollup");
const babel = require("@rollup/plugin-babel");
const resolve = require("@rollup/plugin-node-resolve");
const commonjs = require("@rollup/plugin-commonjs");
const terser = require("@rollup/plugin-terser");
const vue = require("@vitejs/plugin-vue");
const replace = require("@rollup/plugin-replace");

const sassGlobPattern = "src/main/resources/scss/*.scss";
const sassOptions = {
  outputStyle: "compressed",
  includePath: "src/main/resources/scss"
};

function compileSass(exitOnError) {
  let sassTask = sass(sassOptions);

  // Without an error handler specified, the task will exit on error, which we want for the "buildAll" task
  if(!exitOnError) sassTask = sassTask.on("error", sass.logError);

  return gulp.src(sassGlobPattern, {base: "."})
    .pipe(sourcemaps.init())
    .pipe(sassTask)
    .pipe(postcss([autoprefixer({ grid: true })])) // Add the plugin here instead
    .pipe(sourcemaps.write("./"))
    .pipe(rename(path => {
      // E.g. src\main\resources\scss -> src\main\resources\public\assets\static\css
      path.dirname = path.dirname.replace(/([\/\\])scss[\/\\]?/, "$1public$1assets$1static$1css");
    }))
    .pipe(gulp.dest("./"))
}

const babelOptions =
  {
    "exclude": "node_modules/**",
    "presets": [
      ["@babel/preset-env", {
        "useBuiltIns": "usage",
        "corejs": "3",
        "debug": false,
        "targets": {
          "chrome": 65,
          "ie": 11,
          "firefox": 52,
          "safari": 11,
          "ios": 12,
          "edge": 16
        }
      }]
    ],
    "babelHelpers": "bundled",
    "plugins": ["@babel/plugin-transform-class-properties"],
  }

// tasks
gulp.task("rollup-babel", () => rollup.rollup({
    input: "./src/main/resources/js/all.js",
    plugins: [
      replace({
        "process.env.NODE_ENV": JSON.stringify("production"),
      }),
      resolve(),
      vue(),
      commonjs(),
      babel(babelOptions),
      terser(),
    ]
  }).then(bundle => {
    return bundle.write({
      file: "./src/main/resources/public/assets/static/js/fcs-bundle.js",
      format: "iife",
      sourcemap: true
    })
  })
);

// copy FDS images into public/assets
gulp.task('copyFdsImages', () => {
  return gulp.src(['fivium-design-system-core/fds/static/images/**/*'])
    .pipe(gulp.dest('src/main/resources/public/assets/static/fds/images'));
});

// copy FDS into public/assets
gulp.task("copyFdsResources", () => {
  return gulp.src(["fivium-design-system-core/fds/**/*"])
    .pipe(gulp.dest("src/main/resources/templates/fds"));
});

// copy govuk-frontend into public/assets
gulp.task("copyGovukResources", () => {
  return gulp.src(["fivium-design-system-core/node_modules/govuk-frontend/**/*"])
    .pipe(gulp.dest("src/main/resources/public/assets/govuk-frontend"));
});

// copy FDS bundle into public/assets
gulp.task("copyJs", () => {
  return gulp.src(["src/main/resources/templates/fds/static/js/**/*"])
    .pipe(gulp.dest("src/main/resources/public/assets/static/fds/js"));
});

// copy vendor JS into public/assets
gulp.task("copyVendorJs", () => {
  return gulp.src(["src/main/resources/templates/fds/vendor/**/*"])
    .pipe(gulp.dest("src/main/resources/public/assets/static/js/vendor"))
});

// copy govuk-frontend dependency into public/assets
gulp.task("copyHtml5Shiv", () => {
  return gulp.src(["fivium-design-system-core/node_modules/html5shiv/dist/html5shiv.min.js"])
    .pipe(gulp.dest("src/main/resources/public/assets/html5shiv"))
});

// Init all appropriate resources into projects public/assets
gulp.task("initFds", gulp.series(["copyFdsResources", "copyFdsImages", "copyGovukResources", "copyHtml5Shiv", "copyJs", "copyVendorJs"]))

gulp.task("sassCi", gulp.series(["initFds"], () => {
  return compileSass(true);
}));

gulp.task("buildAll", gulp.series(["sassCi", "rollup-babel"]));
