import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Grid, Box, Typography, Button, Container, Card, CardContent, CardActions, Paper, Avatar } from '@mui/material';
import {
  Search,
  Build,
  LocationOn,
  Star,
  Phone,
  AccessTime,
  ArrowForward
} from '@mui/icons-material';

export default function Home() {
  const { t, i18n } = useTranslation();

  const features = [
    {
      icon: <Search sx={{ fontSize: 40 }} />,
      title: i18n.language === 'ar' ? 'بحث سهل' : 'Easy Search',
      description: i18n.language === 'ar'
        ? 'ابحث عن الفنيين في منطقتك بسهولة'
        : 'Find technicians in your area easily'
    },
    {
      icon: <LocationOn sx={{ fontSize: 40 }} />,
      title: i18n.language === 'ar' ? 'بحث بالموقع' : 'Location-Based',
      description: i18n.language === 'ar'
        ? 'احصل على الفنيين الأقرب إليك'
        : 'Get technicians nearest to you'
    },
    {
      icon: <Star sx={{ fontSize: 40 }} />,
      title: i18n.language === 'ar' ? 'تقييمات موثوقة' : 'Trusted Reviews',
      description: i18n.language === 'ar'
        ? 'اطلع على تقييمات العملاء السابقين'
        : 'Check reviews from previous customers'
    },
    {
      icon: <Phone sx={{ fontSize: 40 }} />,
      title: i18n.language === 'ar' ? 'اتصال مباشر' : 'Direct Contact',
      description: i18n.language === 'ar'
        ? 'تواصل مباشرة عبر الهاتف أو واتساب'
        : 'Contact directly via phone or WhatsApp'
    }
  ];

  const services = [
    i18n.language === 'ar' ? 'سباكة' : 'Plumbing',
    i18n.language === 'ar' ? 'كهرباء' : 'Electrical',
    i18n.language === 'ar' ? 'تكييف' : 'Air Conditioning',
    i18n.language === 'ar' ? 'نجارة' : 'Carpentry',
    i18n.language === 'ar' ? 'دهانات' : 'Painting',
    i18n.language === 'ar' ? 'أجهزة منزلية' : 'Home Appliances'
  ];

  return (
    <Box>
      {/* Hero Section */}
      <Paper
        sx={{
          position: 'relative',
          backgroundColor: 'primary.main',
          color: '#fff',
          mb: 4,
          backgroundSize: 'cover',
          backgroundRepeat: 'no-repeat',
          backgroundPosition: 'center',
          borderRadius: 2,
          overflow: 'hidden'
        }}
      >
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            bottom: 0,
            right: 0,
            left: 0,
            backgroundColor: 'rgba(0,0,0,.3)',
          }}
        />
        <Container maxWidth="lg" sx={{ position: 'relative', py: 8 }}>
          <Grid container spacing={4} alignItems="center" sx={{ width: '100%' }}>
            <Grid size={{ xs: 12, md: 7 }}>
              <Typography 
                component="h1" 
                variant="h2" 
                gutterBottom
                sx={{ fontWeight: 'bold' }}
              >
                {t('title')}
              </Typography>
              <Typography variant="h5" paragraph sx={{ mb: 4 }}>
                {i18n.language === 'ar' 
                  ? 'ابحث عن أفضل الفنيين في منطقتك'
                  : 'Find the best technicians in your area'}
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <Button
                  component={Link}
                  to="/search"
                  variant="contained"
                  size="large"
                  startIcon={<Search />}
                  sx={{ 
                    bgcolor: 'white',
                    color: 'primary.main',
                    '&:hover': {
                      bgcolor: 'grey.100'
                    }
                  }}
                >
                  {t('search')}
                </Button>
                <Button
                  component={Link}
                  to="/register"
                  variant="outlined"
                  size="large"
                  startIcon={<ArrowForward />}
                  sx={{ 
                    borderColor: 'white',
                    color: 'white',
                    '&:hover': {
                      borderColor: 'white',
                      bgcolor: 'rgba(255,255,255,0.1)'
                    }
                  }}
                >
                  {t('register')}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </Container>
      </Paper>

      {/* Features Section */}
      <Container maxWidth="lg" sx={{ mb: 6 }}>
        <Typography 
          variant="h4" 
          align="center" 
          gutterBottom 
          sx={{ mb: 4, fontWeight: 'bold' }}
        >
          {i18n.language === 'ar' ? 'لماذا تختارنا؟' : 'Why Choose Us?'}
        </Typography>
        <Grid container spacing={3} sx={{ width: '100%' }}>
          {features.map((feature, index) => (
            <Grid key={index} size={{ xs: 12, sm: 6, md: 3 }}>
              <Card 
                sx={{ 
                  height: '100%',
                  textAlign: 'center',
                  transition: 'transform 0.3s',
                  '&:hover': {
                    transform: 'translateY(-8px)',
                    boxShadow: 4
                  }
                }}
              >
                <CardContent>
                  <Avatar
                    sx={{
                      bgcolor: 'primary.main',
                      width: 70,
                      height: 70,
                      margin: '0 auto 16px'
                    }}
                  >
                    {feature.icon}
                  </Avatar>
                  <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold' }}>
                    {feature.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {feature.description}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* Services Section */}
      <Container maxWidth="lg" sx={{ mb: 6 }}>
        <Typography 
          variant="h4" 
          align="center" 
          gutterBottom 
          sx={{ mb: 4, fontWeight: 'bold' }}
        >
          {i18n.language === 'ar' ? 'الخدمات المتاحة' : 'Available Services'}
        </Typography>
        <Grid container spacing={2} justifyContent="center" sx={{ width: '100%' }}>
          {services.map((service, index) => (
            <Grid key={index}>
              <Button
                variant="outlined"
                size="large"
                startIcon={<Build />}
                component={Link}
                to={`/search?field=${service}`}
                sx={{ 
                  borderRadius: 8,
                  px: 3,
                  py: 1.5
                }}
              >
                {service}
              </Button>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* CTA Section */}
      <Paper
        sx={{
          backgroundColor: 'grey.100',
          borderRadius: 2,
          p: 4,
          textAlign: 'center'
        }}
      >
        <Container maxWidth="md">
          <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
            {i18n.language === 'ar' 
              ? 'جاهز للبدء؟'
              : 'Ready to Get Started?'}
          </Typography>
          <Typography variant="body1" paragraph color="text.secondary">
            {i18n.language === 'ar'
              ? 'ابحث عن أفضل الفنيين في منطقتك الآن'
              : 'Find the best technicians in your area now'}
          </Typography>
          <Button
            component={Link}
            to="/search"
            variant="contained"
            size="large"
            startIcon={<Search />}
          >
            {i18n.language === 'ar' ? 'ابدأ البحث' : 'Start Searching'}
          </Button>
        </Container>
      </Paper>
    </Box>
  );
}
