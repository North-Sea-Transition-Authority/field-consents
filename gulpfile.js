const sass = require('gulp-sass')(require('sass'));
const postcss = require('gulp-postcss');
const gulp = require('gulp');
const sourcemaps = require('gulp-sourcemaps');
const autoprefixer = require('autoprefixer');
const rename = require("gulp-rename");

const sassGlobPattern = "src/main/resources/scss/*.scss";
const sassOptions = {
  outputStyle: 'compressed',
  includePath: 'src/main/resources/scss'
};

function compileSass(exitOnError) {
  let sassTask = sass(sassOptions);

  // Without an error handler specified, the task will exit on error, which we want for the "buildAll" task
  if(!exitOnError) sassTask = sassTask.on('error', sass.logError);

  return gulp.src(sassGlobPattern, {base: '.'})
    .pipe(sourcemaps.init())
    .pipe(sassTask)
    .pipe(postcss([autoprefixer({ grid: true })])) // Add the plugin here instead
    .pipe(sourcemaps.write('./'))
    .pipe(rename(path => {
      // E.g. src\main\resources\scss -> src\main\resources\public\assets\static\css
      path.dirname = path.dirname.replace(/([\/\\])templates([\/\\])docs/, '$1public$1assets$1static$1css');
    }))
    .pipe(gulp.dest('./'))
}

// copy FDS into public/assets
gulp.task('copyFdsResources', () => {
  return gulp.src(['fivium-design-system-core/fds/**/*'])
    .pipe(gulp.dest('src/main/resources/templates/fds'));
});

// copy govuk-frontend into public/assets
gulp.task('copyGovukResources', () => {
  return gulp.src(['fivium-design-system-core/node_modules/govuk-frontend/**/*'])
    .pipe(gulp.dest('src/main/resources/public/assets/govuk-frontend'));
});

// copy FDS bundle into public/assets
gulp.task('copyJs', () => {
  return gulp.src(['src/main/resources/templates/fds/static/js/**/*'])
    .pipe(gulp.dest('src/main/resources/public/assets/static/fds/js'));
});

// copy vendor JS into public/assets
gulp.task('copyVendorJs', () => {
  return gulp.src(['src/main/resources/templates/fds/vendor/**/*'])
    .pipe(gulp.dest('src/main/resources/public/assets/static/js/vendor'))
});

// copy govuk-frontend dependency into public/assets
gulp.task('copyHtml5Shiv', () => {
  return gulp.src(['fivium-design-system-core/node_modules/html5shiv/dist/html5shiv.min.js'])
    .pipe(gulp.dest('src/main/resources/public/assets/html5shiv'))
});

// Init all appropriate resources into project's public/assets
gulp.task('initFds', gulp.series(['copyFdsResources', 'copyGovukResources', 'copyHtml5Shiv', 'copyJs', 'copyVendorJs']))

gulp.task('sassCi', gulp.series(['initFds'], () => {
  return compileSass(true);
}));

gulp.task('buildAll', gulp.series(['sassCi']));
